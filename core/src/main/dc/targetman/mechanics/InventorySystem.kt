package dc.targetman.mechanics

import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.PickupPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.weapon.Weapon
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
                    grip(weapon, skeletonPart, inventoryPart)
                    val weaponLimb = skeletonPart[inventoryPart.weaponLimbName]
                    val removedWeaponTransform = weaponLimb.transform as Box2dTransform
                    pickupFactory.create(removedWeapon, removedWeaponTransform)
                }
                entityManager.remove(pickup.entity)
            }
        }
    }

    private fun grip(weapon: Weapon, skeletonPart: SkeletonPart, inventoryPart: InventoryPart) {
//        val skeleton = SkeletonUtils.createSkeleton(weapon.data.skeletonPath, weapon.data.atlasName)
//        val skeletonPartFactory = SkeletonPartFactory()
//        val skeletonScale = weapon.data.width / skeleton.bounds.size.x
//        val size = skeleton.bounds.size.scl(skeletonScale)
//        val weaponSkeletonPart = skeletonPartFactory.create(skeleton, weapon.data.atlasName, size)
        // TODO:
        // Make the gun a separate entity
        // 1. Lets get the hand bone position from the skeleton
        // 2. Put the gun's grip position at the hand bone position.  Make the rotation and scale the same as the hand's
    }
}