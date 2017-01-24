package dc.targetman.mechanics

import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.PickupPart
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.geometry.toVector3
import dclib.physics.collision.CollisionChecker

class InventorySystem(
        private val entityManager: EntityManager,
        private val collisionChecker: CollisionChecker,
        private val pickupFactory: PickupFactory
) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val inventoryPart = entity.tryGet(InventoryPart::class)
        if (inventoryPart != null) {
            inventoryPart.pickupTimer.tick(delta)
            if (inventoryPart.pickup) {
                tryPickup(entity[SkeletonPart::class], inventoryPart)
                inventoryPart.pickup = false
            }
        }
    }

    private fun tryPickup(skeletonPart: SkeletonPart, inventoryPart: InventoryPart) {
        for (limb in skeletonPart.getActiveLimbs()) {
            val targets = collisionChecker.getTargets(limb.entity)
            val pickup = targets.firstOrNull { it.entity.has(PickupPart::class) }
            if (pickup != null && inventoryPart.pickupTimer.check()) {
                val weapon = pickup.entity[PickupPart::class].weapon
                val removedWeapon = inventoryPart.pickup(weapon)
                if (removedWeapon != null) {
                    val removedWeaponTransform = skeletonPart[inventoryPart.weaponLimbName].transform
                    val pickupPosition = removedWeaponTransform.center.toVector3(removedWeaponTransform.z)
                    pickupFactory.create(removedWeapon, pickupPosition)
                }
                entityManager.remove(pickup.entity)
            }
        }
    }
}