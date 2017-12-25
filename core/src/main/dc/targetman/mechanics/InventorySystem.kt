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
        factoryTools.entityManager.entityDestroyed.on { inventoryActions.tryDropEquippedWeapon(it.entity) }
        collisionChecker.collided.on { inventoryActions.tryPickup(it) }
    }

    override fun update(delta: Float, entity: Entity) {
        val inventoryPart = entity.tryGet(InventoryPart::class)
        if (inventoryPart != null) {
            updateSwitching(delta, entity)
            inventoryPart.pickupTimer.tick(delta)
        }
    }

    private fun updateSwitching(delta: Float, entity: Entity) {
        val inventoryPart = entity[InventoryPart::class]
        inventoryPart.switchTimer.tick(delta)
        val actionsPart = entity[ActionsPart::class]
        if (actionsPart[ActionKey.SWITCH_WEAPON].doing && inventoryPart.switchTimer.check()) {
            inventoryPart.switchWeapon()
            val gripper = entity[SkeletonPart::class][inventoryPart.gripperName]
            inventoryActions.gripCurrentWeapon(inventoryPart, gripper)
        }
    }
}