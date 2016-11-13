package dc.targetman.mechanics.weapon

import dc.targetman.epf.parts.WeaponPart
import dc.targetman.level.EntityFactory
import dc.targetman.physics.PhysicsUtils
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.LimbsPart
import dclib.geometry.VectorUtils
import dclib.util.FloatRange

class WeaponSystem(private val entityManager: EntityManager, private val entityFactory: EntityFactory)
: EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val weaponPart = entity.tryGet(WeaponPart::class.java)
        if (weaponPart != null && hasFiringLimbs(entity)) {
            fire(weaponPart)
            weaponPart.update(delta)
            weaponPart.setTriggered(false)
        }
    }

    private fun hasFiringLimbs(entity: Entity): Boolean {
        val limbs = entity[LimbsPart::class.java].all
        val firingLimbs = entity[WeaponPart::class.java].rotatorLimb.descendants
        return limbs.containsAll(firingLimbs)
    }

    private fun fire(weaponPart: WeaponPart) {
        if (weaponPart.shouldFire()) {
            val weapon = weaponPart.weapon
            val spreadRange = FloatRange(-weapon.spread / 2, weapon.spread / 2)
            val recoil = VectorUtils.toVector2(weaponPart.centrum.rotation, weapon.recoil).scl(-1f)
            PhysicsUtils.applyForce(entityManager.all, weaponPart.rotatorLimb.entity, recoil)
            for (i in 0..weapon.numBullets - 1) {
                val angleOffset = spreadRange.random()
                val speed = weapon.speedRange.random()
                entityFactory.createBullet(weaponPart.centrum, angleOffset, speed, weapon.bulletType)
            }
            weaponPart.reset()
        }
    }
}
