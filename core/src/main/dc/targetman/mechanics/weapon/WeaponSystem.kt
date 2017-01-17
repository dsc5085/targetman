package dc.targetman.mechanics.weapon

import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.WeaponPart
import dc.targetman.level.EntityFactory
import dc.targetman.mechanics.Alliance
import dc.targetman.physics.PhysicsUtils
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.geometry.VectorUtils
import dclib.physics.Transform
import dclib.util.FloatRange

class WeaponSystem(private val entityManager: EntityManager, private val entityFactory: EntityFactory)
: EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val weaponPart = entity.tryGet(WeaponPart::class)
        val skeletonPart = entity.tryGet(SkeletonPart::class)
        if (weaponPart != null && hasFiringLimbs(weaponPart, skeletonPart!!)) {
            skeletonPart.playAnimation("aim", 1)
            aim(delta, weaponPart)
            fire(entity)
            weaponPart.update(delta)
            weaponPart.setTriggered(false)
        }
    }

    private fun hasFiringLimbs(weaponPart: WeaponPart, skeletonPart: SkeletonPart): Boolean {
        val rotatorName = weaponPart.rotatorName
        val rotatorLimb = skeletonPart[rotatorName]
        return rotatorLimb.getDescendants().any { it.name == weaponPart.muzzleName }
    }

    private fun aim(delta: Float, weaponPart: WeaponPart) {
        val aimSpeed = 180f
        weaponPart.aimRotation += aimSpeed * delta * weaponPart.aimDirection
    }

    private fun fire(entity: Entity) {
        val weaponPart = entity[WeaponPart::class]
        if (weaponPart.shouldFire()) {
            val weapon = weaponPart.weapon
            val muzzleLimb = entity[SkeletonPart::class][weaponPart.muzzleName]
            val muzzleTransform = muzzleLimb.transform
            val recoil = VectorUtils.toVector2(muzzleTransform.rotation, weapon.recoil).scl(-1f)
            PhysicsUtils.applyForce(entityManager.all, entity, recoil)
            createBullets(muzzleTransform, weapon, entity.getAttribute(Alliance::class)!!)
            weaponPart.reset()
        }
    }

    private fun createBullets(muzzleTransform: Transform, weapon: Weapon, alliance: Alliance) {
        val spreadRange = FloatRange(-weapon.spread / 2, weapon.spread / 2)
        for (i in 0..weapon.numBullets - 1) {
            val angleOffset = spreadRange.random()
            val speed = weapon.speedRange.random()
            entityFactory.createBullet(weapon.bullet, muzzleTransform, angleOffset, speed, alliance)
        }
    }
}