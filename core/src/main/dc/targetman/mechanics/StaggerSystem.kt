package dc.targetman.mechanics

import com.badlogic.gdx.physics.box2d.World
import dc.targetman.epf.parts.ForcePart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.StaggerPart
import dc.targetman.skeleton.LimbUtils
import dc.targetman.skeleton.RagdollFactory
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dUtils
import dclib.physics.collision.CollidedEvent
import dclib.physics.collision.CollisionChecker

class StaggerSystem(private val entityManager: EntityManager, world: World, collisionChecker: CollisionChecker)
    : EntitySystem(entityManager) {
    val ragdollFactory = RagdollFactory(world)

    init {
        collisionChecker.collided.on { handleCollided(it) }
    }

    override fun update(delta: Float, entity: Entity) {
        val staggerPart = entity.tryGet(StaggerPart::class)
        if (staggerPart != null && staggerPart.isStaggered) {
            if (staggerPart.staggerTimer.check()) {
                recover(entity)
            }
            staggerPart.staggerTimer.tick(delta)
        }
    }

    private fun handleCollided(event: CollidedEvent) {
        val forcePart = event.source.entity.tryGet(ForcePart::class)
        val container = LimbUtils.findContainer(entityManager.getAll(), event.target.entity)
        val staggerPart = container?.tryGet(StaggerPart::class)
        if (forcePart != null && staggerPart != null) {
            if (!staggerPart.isStaggered && forcePart.force >= staggerPart.minForce) {
                val skeletonPart = container[SkeletonPart::class]
                staggerPart.isStaggered = true
                skeletonPart.isEnabled = false
                Box2dUtils.getBody(container)!!.isActive = false
                ragdoll(skeletonPart, staggerPart)
            }
        }
    }

    private fun ragdoll(skeletonPart: SkeletonPart, staggerPart: StaggerPart) {
        val ragdollLimbs = ragdollFactory.create(skeletonPart.root).getDescendants(includeInactive = true)
        for (limb in skeletonPart.getLimbs()) {
            Box2dUtils.getBody(limb.entity)?.isActive = false
            staggerPart.oldLimbTransforms.put(limb.name, limb.transform)
            val transformPart = limb.entity[TransformPart::class]
            transformPart.transform = ragdollLimbs.first { it.name == limb.name }.transform
        }
    }

    private fun recover(entity: Entity) {
        val skeletonPart = entity[SkeletonPart::class]
        val staggerPart = entity[StaggerPart::class]
        restoreSkeletonLimbs(skeletonPart, staggerPart)
        Box2dUtils.getBody(entity)!!.isActive = true
        skeletonPart.isEnabled = true
        staggerPart.isStaggered = false
        // animate to standing back up
    }

    private fun restoreSkeletonLimbs(skeletonPart: SkeletonPart, staggerPart: StaggerPart) {
        for (limb in skeletonPart.getLimbs()) {
            val transformPart = limb.entity[TransformPart::class]
            Box2dUtils.tryDestroyBody(transformPart.transform)
            transformPart.transform = staggerPart.oldLimbTransforms[limb.name]!!
        }
        staggerPart.oldLimbTransforms.clear()
    }
}