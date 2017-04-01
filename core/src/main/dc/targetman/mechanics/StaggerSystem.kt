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
import dclib.physics.Box2dTransform
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
                staggerPart.isStaggered = false
                // turn limbs back into skeleton limbs
                // animate to standing back up
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
                (container[TransformPart::class].transform as Box2dTransform).body.isActive = false
                ragdoll(skeletonPart)
            }
        }
    }

    private fun ragdoll(skeletonPart: SkeletonPart) {
        val ragdollLimbs = ragdollFactory.create(skeletonPart.root).getDescendants(includeInactive = true)
        for (limb in skeletonPart.getLimbs()) {
            val transformPart = limb.entity[TransformPart::class]
            val oldTransform = transformPart.transform
            transformPart.transform = ragdollLimbs.first { it.name == limb.name }.transform
            if (oldTransform is Box2dTransform) {
                oldTransform.body.isActive = false
            }
        }
        skeletonPart.isEnabled = false
    }
}