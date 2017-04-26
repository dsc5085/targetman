package dc.targetman.mechanics

import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.StaggerPart
import dc.targetman.level.FactoryTools
import dc.targetman.skeleton.LimbUtils
import dc.targetman.skeleton.RagdollFactory
import dclib.epf.Entity
import dclib.epf.EntityRemovedEvent
import dclib.epf.EntitySystem
import dclib.epf.parts.CollisionDamagePart
import dclib.epf.parts.TransformPart
import dclib.geometry.base
import dclib.physics.Box2dUtils
import dclib.physics.collision.CollidedEvent
import dclib.physics.collision.CollisionChecker

class StaggerSystem(private val factoryTools: FactoryTools, collisionChecker: CollisionChecker)
    : EntitySystem(factoryTools.entityManager) {
    val ragdollFactory = RagdollFactory(factoryTools.world)
    val inventoryActions = InventoryActions(factoryTools)

    init {
        factoryTools.entityManager.entityRemoved.on { handleEntityRemoved(it) }
        collisionChecker.collided.on { handleCollided(it) }
    }

    override fun update(delta: Float, entity: Entity) {
        val staggerPart = entity.tryGet(StaggerPart::class)
        if (staggerPart != null) {
            changeStaggerAmount(entity, -staggerPart.recoverySpeed * delta)
        }
    }

    private fun handleEntityRemoved(event: EntityRemovedEvent) {
        val staggerPart = event.entity.tryGet(StaggerPart::class)
        if (staggerPart != null) {
            for (limbTransform in staggerPart.oldLimbTransforms.values) {
                Box2dUtils.tryDestroyBody(limbTransform)
            }
        }
    }

    private fun handleCollided(event: CollidedEvent) {
        val damagePart = event.source.entity.tryGet(CollisionDamagePart::class)
        val entities = factoryTools.entityManager.getAll()
        val container = LimbUtils.findContainer(entities, event.target.entity)
        val staggerPart = container?.tryGet(StaggerPart::class)
        if (damagePart != null && staggerPart != null) {
            changeStaggerAmount(container, damagePart.damage)
        }
    }

    private fun changeStaggerAmount(entity: Entity, offset: Float) {
        val staggerPart = entity[StaggerPart::class]
        val oldState = staggerPart.state
        staggerPart.amount = Math.max(staggerPart.amount + offset, 0f)
        val newState = staggerPart.state
        if (oldState == StaggerState.DOWN && newState != oldState) {
            recover(entity)
        } else if (newState == StaggerState.DOWN && oldState != newState) {
            knockdown(entity)
        }
        if (newState == StaggerState.HURT) {
            stun(entity)
        }
    }

    private fun recover(entity: Entity) {
        val skeletonPart = entity[SkeletonPart::class]
        val staggerPart = entity[StaggerPart::class]
        Box2dUtils.getBody(entity)!!.isActive = true
        val transform = entity[TransformPart::class].transform
        transform.setWorld(transform.bounds.base, skeletonPart.root.transform.center)
        skeletonPart.isEnabled = true
        restoreSkeletonLimbs(skeletonPart, staggerPart)
    }

    private fun restoreSkeletonLimbs(skeletonPart: SkeletonPart, staggerPart: StaggerPart) {
        for (limb in skeletonPart.getLimbs()) {
            val transformPart = limb.entity[TransformPart::class]
            Box2dUtils.tryDestroyBody(transformPart.transform)
            transformPart.transform = staggerPart.oldLimbTransforms[limb.name]!!
            Box2dUtils.getBody(limb.entity)?.isActive = true
        }
        staggerPart.oldLimbTransforms.clear()
    }

    private fun knockdown(entity: Entity) {
        val skeletonPart = entity[SkeletonPart::class]
        inventoryActions.tryDropEquippedWeapon(entity)
        ragdoll(skeletonPart, entity[StaggerPart::class])
        skeletonPart.isEnabled = false
        Box2dUtils.getBody(entity)!!.isActive = false
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

    private fun stun(entity: Entity) {
        entity[SkeletonPart::class].playAnimation("hurt", 1)
    }
}