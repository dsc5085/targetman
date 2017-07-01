package dc.targetman.mechanics

import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.StaggerPart
import dc.targetman.level.FactoryTools
import dc.targetman.skeleton.LimbUtils
import dc.targetman.skeleton.Ragdoller
import dclib.epf.Entity
import dclib.epf.EntitySystem
import dclib.epf.parts.CollisionDamagePart
import dclib.epf.parts.TransformPart
import dclib.geometry.base
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.collision.CollidedEvent
import dclib.physics.collision.CollisionChecker

class StaggerSystem(private val factoryTools: FactoryTools, collisionChecker: CollisionChecker)
    : EntitySystem(factoryTools.entityManager) {
    val inventoryActions = InventoryActions(factoryTools)

    init {
        collisionChecker.collided.on { handleCollided(it) }
    }

    override fun update(delta: Float, entity: Entity) {
        val staggerPart = entity.tryGet(StaggerPart::class)
        if (staggerPart != null) {
            changeStaggerAmount(entity, -staggerPart.recoverySpeed * delta)
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
        transform.setWorld(transform.bounds.base, skeletonPart.root.limb.transform.center)
        skeletonPart.isEnabled = true
        restoreSkeletonLimbs(skeletonPart, staggerPart)
    }

    private fun restoreSkeletonLimbs(skeletonPart: SkeletonPart, staggerPart: StaggerPart) {
        for (limb in skeletonPart.getLimbs()) {
            val transform = limb.transform
            if (transform is Box2dTransform) {
                Box2dUtils.setSensor(transform.body, true)
            }
        }
    }

    private fun knockdown(entity: Entity) {
        val skeletonPart = entity[SkeletonPart::class]
        inventoryActions.tryDropEquippedWeapon(entity)
        Ragdoller.ragdoll(skeletonPart.root.limb)
        skeletonPart.isEnabled = false
        Box2dUtils.getBody(entity)!!.isActive = false
    }

    private fun stun(entity: Entity) {
        entity[SkeletonPart::class].playAnimation("hurt", 1)
    }
}