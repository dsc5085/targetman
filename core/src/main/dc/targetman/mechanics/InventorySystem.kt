package dc.targetman.mechanics

import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.level.FactoryTools
import dclib.epf.Entity
import dclib.epf.EntitySystem
import dclib.physics.collision.CollisionChecker

class InventorySystem(factoryTools: FactoryTools, collisionChecker: CollisionChecker)
    : EntitySystem(factoryTools.entityManager) {
    private val inventoryActions = InventoryActions(factoryTools)

    init {
        factoryTools.entityManager.entityAdded.on { inventoryActions.tryEquipCurrentWeapon(it.entity) }
        factoryTools.entityManager.entityRemoved.on { inventoryActions.tryDropEquippedWeapon(it.entity) }
        collisionChecker.collided.on { inventoryActions.tryPickup(it) }
    }

    override fun update(delta: Float, entity: Entity) {
        val inventoryPart = entity.tryGet(InventoryPart::class)
        if (inventoryPart != null) {
            updateSwitching(delta, inventoryPart, entity[SkeletonPart::class])
            inventoryPart.pickupTimer.tick(delta)
        }
    }

    private fun updateSwitching(delta: Float, inventoryPart: InventoryPart, skeletonPart: SkeletonPart) {
        inventoryPart.switchTimer.tick(delta)
        if (inventoryPart.trySwitchWeapon && inventoryPart.switchTimer.check()) {
            inventoryPart.switchWeapon()
            val gripper = skeletonPart[inventoryPart.gripperName]
            inventoryActions.gripEquippedWeapon(inventoryPart, gripper)
        }
    }
}