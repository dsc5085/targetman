package dc.targetman.mechanics

import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.PickupPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.skeleton.LimbFactory
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.collision.CollisionChecker

class InventorySystem(
        private val entityManager: EntityManager,
        private val collisionChecker: CollisionChecker,
        private val pickupFactory: PickupFactory,
        private val limbFactory: LimbFactory
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
                val newWeapon = pickup.entity[PickupPart::class].weapon
                val removedWeapon = inventoryPart.pickup(newWeapon)
                if (removedWeapon != null) {
                    pickup(inventoryPart, removedWeapon, skeletonPart, newWeapon)
                }
                entityManager.remove(pickup.entity)
            }
        }
    }

    private fun pickup(
            inventoryPart: InventoryPart,
            removedWeapon: Weapon,
            skeletonPart: SkeletonPart,
            newWeapon: Weapon
    ) {
        val gripper = skeletonPart[inventoryPart.gripperName]
        val weaponLink = limbFactory.link(newWeapon.skeleton, newWeapon.data.atlasName, newWeapon.size, gripper)
        val newWeaponEntity = Entity(SkeletonPart(weaponLink.root), TransformPart(weaponLink.transform))
        entityManager.add(newWeaponEntity)
        val gripperDescendants = gripper.getDescendants(includeLinked = true)
        // TODO: Implement better check to get weapon transform
        val transformLimb = gripperDescendants.firstOrNull { it.entity.has(SpritePart::class) }
        if (transformLimb != null) {
            val removedWeaponTransform = transformLimb.transform as Box2dTransform
            pickupFactory.create(removedWeapon, Box2dTransform(removedWeaponTransform))
        }
    }
}