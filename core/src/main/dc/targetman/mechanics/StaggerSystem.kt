package dc.targetman.mechanics

import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.StaggerPart
import dc.targetman.level.FactoryTools
import dc.targetman.skeleton.Ragdoller
import dclib.epf.Entity
import dclib.epf.EntitySystem
import dclib.epf.parts.HealthPart
import dclib.epf.parts.TransformPart
import dclib.geometry.base
import dclib.mechanics.HealthChangedEvent
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class StaggerSystem(factoryTools: FactoryTools) : EntitySystem(factoryTools.entityManager) {
    val inventoryActions = InventoryActions(factoryTools)

    init {
        factoryTools.entityManager.entityAdded.on {
            val entity = it.entity
            val healthPart = entity.tryGet(HealthPart::class)
            if (healthPart != null) {
                healthPart.healthChanged.on { handleHealthChanged(entity, it) }
            }
        }
    }

    override fun update(delta: Float, entity: Entity) {
        val staggerPart = entity.tryGet(StaggerPart::class)
        if (staggerPart != null) {
            changeStaggerAmount(entity, -staggerPart.recoverySpeed * delta)
        }
    }

    private fun handleHealthChanged(entity: Entity, event: HealthChangedEvent) {
        val staggerPart = entity.tryGet(StaggerPart::class)
        if (staggerPart != null && event.offset < 0) {
            changeStaggerAmount(entity, -event.offset)
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
            entity[SkeletonPart::class].playAnimation("hurt", 1)
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
}