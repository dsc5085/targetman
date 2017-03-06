package dc.targetman.mechanics

import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.PickupPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.level.FactoryTools
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.skeleton.Limb
import dc.targetman.skeleton.LimbFactory
import dclib.epf.Entity
import dclib.epf.EntitySystem
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.collision.CollisionChecker

class InventorySystem(factoryTools: FactoryTools, private val collisionChecker: CollisionChecker)
    : EntitySystem(factoryTools.entityManager) {
    private val entityManager = factoryTools.entityManager
    private val limbFactory = LimbFactory(factoryTools)
    private val pickupFactory = PickupFactory(factoryTools)

    init {
        entityManager.entityAdded.on {
            val inventoryPart = it.entity.tryGet(InventoryPart::class)
            if (inventoryPart != null) {
                val gripper = it.entity[SkeletonPart::class][inventoryPart.gripperName]
                equipCurrentWeapon(inventoryPart, gripper)
            }
        }
    }

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
                val gripper = skeletonPart[inventoryPart.gripperName]
                equipCurrentWeapon(inventoryPart, gripper)
                if (removedWeapon != null) {
                    drop(removedWeapon, gripper)
                }
                entityManager.remove(pickup.entity)
            }
        }
    }

    private fun equipCurrentWeapon(inventoryPart: InventoryPart, gripper: Limb) {
        val equippedWeapon = inventoryPart.equippedWeapon
        val weaponLink = limbFactory.link(
                equippedWeapon.skeleton,
                equippedWeapon.data.atlasName,
                equippedWeapon.size,
                gripper)
        val newWeaponEntity = Entity(SkeletonPart(weaponLink.root), TransformPart(weaponLink.transform))
        entityManager.add(newWeaponEntity)
    }

    private fun drop(weapon: Weapon, gripper: Limb) {
        val gripperDescendants = gripper.getDescendants(includeLinked = true)
        // TODO: Implement better check to get weapon transform
        val transformLimb = gripperDescendants.firstOrNull { it.entity.has(SpritePart::class) }
        if (transformLimb != null) {
            val removedWeaponTransform = transformLimb.transform as Box2dTransform
            pickupFactory.create(weapon, Box2dTransform(removedWeaponTransform))
        }
    }
}