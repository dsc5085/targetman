package dc.targetman.mechanics.weapon

import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.WeaponPart
import dc.targetman.level.EntityFactory
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem

class WeaponSystem(private val entityManager: EntityManager, private val entityFactory: EntityFactory)
: EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        // TODO:
//        val weaponPart = entity.tryGet(WeaponPart::class.java)
//        if (weaponPart != null && hasFiringLimbs(entity)) {
//            fire(weaponPart)
//            weaponPart.update(delta)
//            weaponPart.setTriggered(false)
//        }
    }

    private fun hasFiringLimbs(entity: Entity): Boolean {
        val rotatorName = entity[WeaponPart::class.java].rotatorName
        return entity[SkeletonPart::class.java].getDescendants(rotatorName).all { it.isActive }
    }

    private fun fire(weaponPart: WeaponPart) {
        // TODO:
//        if (weaponPart.shouldFire()) {
//            val weapon = weaponPart.weapon
//            val spreadRange = FloatRange(-weapon.spread / 2, weapon.spread / 2)
//            val recoil = VectorUtils.toVector2(weaponPart.centrum.rotation, weapon.recoil).scl(-1f)
//            PhysicsUtils.applyForce(entityManager.all, weaponPart.rotatorLimb.entity, recoil)
//            for (i in 0..weapon.numBullets - 1) {
//                val angleOffset = spreadRange.random()
//                val speed = weapon.speedRange.random()
//                entityFactory.createBullet(weaponPart.centrum, angleOffset, speed, weapon.bulletType)
//            }
//            weaponPart.reset()
//        }
    }
}
