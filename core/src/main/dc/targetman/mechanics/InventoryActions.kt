package dc.targetman.mechanics

import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.PickupPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.level.FactoryTools
import dc.targetman.skeleton.Limb
import dc.targetman.skeleton.LimbFactory
import dc.targetman.skeleton.LimbUtils
import dc.targetman.skeleton.SkeletonUtils
import dclib.epf.Entity
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.collision.CollidedEvent

class InventoryActions(factoryTools: FactoryTools) {
    private val entityManager = factoryTools.entityManager
    private val limbFactory = LimbFactory(factoryTools)
    private val pickupFactory = PickupFactory(factoryTools)

    fun tryEquipCurrentWeapon(entity: Entity) {
        val inventoryPart = entity.tryGet(InventoryPart::class)
        if (inventoryPart != null) {
            val gripper = entity[SkeletonPart::class][inventoryPart.gripperName]
            gripCurrentWeapon(inventoryPart, gripper)
        }
    }

    fun tryPickup(collidedEvent: CollidedEvent) {
        val pickupEntity = collidedEvent.target.entity
        if (pickupEntity.has(PickupPart::class)) {
            val sourceEntity = LimbUtils.findContainer(entityManager.getAll(), collidedEvent.source.entity)
            if (sourceEntity != null) {
                val inventoryPart = sourceEntity.tryGet(InventoryPart::class)
                if (inventoryPart != null && inventoryPart.tryPickup && inventoryPart.pickupTimer.check()) {
                    pickup(inventoryPart, sourceEntity[SkeletonPart::class], pickupEntity)
                }
            }
        }
    }

    fun tryDropEquippedWeapon(entity: Entity) {
        val inventoryPart = entity.tryGet(InventoryPart::class)
        if (inventoryPart != null && inventoryPart.equippedWeapon != null) {
            val gripper = entity[SkeletonPart::class][inventoryPart.gripperName]
            dropEquippedWeapon(inventoryPart, gripper)
        }
    }

    fun gripCurrentWeapon(inventoryPart: InventoryPart, gripper: Limb) {
        limbFactory.removeChildren(gripper)
        val equippedWeapon = inventoryPart.equippedWeapon
        val rootScale = SkeletonUtils.calculateRootScale(equippedWeapon!!.skeleton, equippedWeapon.size)
        val weaponRoot = limbFactory.link(
                equippedWeapon.skeleton,
                equippedWeapon.data.atlasName,
                rootScale,
                gripper)
        val newWeaponEntity = Entity(SkeletonPart(weaponRoot), TransformPart(weaponRoot.limb.transform))
        entityManager.add(newWeaponEntity)
    }

    private fun pickup(inventoryPart: InventoryPart, skeletonPart: SkeletonPart, pickupEntity: Entity) {
        val pickupPart = pickupEntity[PickupPart::class]
        val gripper = skeletonPart[inventoryPart.gripperName]
        if (inventoryPart.isFull) {
            dropEquippedWeapon(inventoryPart, gripper)
        }
        inventoryPart.pickup(pickupPart.weapon)
        gripCurrentWeapon(inventoryPart, gripper)
        entityManager.remove(pickupEntity)
    }

    private fun dropEquippedWeapon(inventoryPart: InventoryPart, gripper: Limb) {
        val gripperDescendants = gripper.getDescendants(includeLinked = true)
        // TODO: Implement better check to get weapon transform
        val transformLimb = gripperDescendants.firstOrNull { it.entity.has(SpritePart::class) }
        val equippedWeapon = inventoryPart.equippedWeapon
        if (transformLimb != null && equippedWeapon != null) {
            val removedWeaponTransform = transformLimb.transform as Box2dTransform
            pickupFactory.create(equippedWeapon, Box2dTransform(removedWeaponTransform))
        }
        inventoryPart.dropEquippedWeapon()
        limbFactory.removeChildren(gripper)
    }
}