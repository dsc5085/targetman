package dc.targetman.mechanics

import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.PickupPart
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.physics.Box2dTransform
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
        for (limb in skeletonPart.getLimbs()) {
            val targets = collisionChecker.getTargets(limb.entity)
            val pickup = targets.firstOrNull { it.entity.has(PickupPart::class) }
            if (pickup != null && inventoryPart.pickupTimer.check()) {
                val weapon = pickup.entity[PickupPart::class].weapon
                val removedWeapon = inventoryPart.pickup(weapon)
                if (removedWeapon != null) {
                    val weaponLimb = skeletonPart[inventoryPart.weaponLimbName]
                    val removedWeaponTransform = weaponLimb.transform as Box2dTransform
                    pickupFactory.create(removedWeapon, Box2dTransform(removedWeaponTransform))
                }
                entityManager.remove(pickup.entity)
            }
        }
    }
}