package dc.targetman.mechanics.weapon

import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.WeaponPart
import dc.targetman.level.EntityFactory
import dc.targetman.physics.PhysicsUtils
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.geometry.VectorUtils
import dclib.physics.Transform
import dclib.util.FloatRange

class WeaponSystem(private val entityManager: EntityManager, private val entityFactory: EntityFactory)
: EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val weaponPart = entity.tryGet(WeaponPart::class)
        if (weaponPart != null && hasFiringLimbs(entity)) {
            fire(entity)
            weaponPart.update(delta)
            weaponPart.setTriggered(false)
        }
    }

    private fun hasFiringLimbs(entity: Entity): Boolean {
        val rotatorName = entity[WeaponPart::class].rotatorName
        return entity[SkeletonPart::class].getDescendants(rotatorName).all { it.isActive }
    }

    private fun fire(entity: Entity) {
        val weaponPart = entity[WeaponPart::class]
        if (weaponPart.shouldFire()) {
            val weapon = weaponPart.weapon
            val skeletonPart = entity[SkeletonPart::class]
            val muzzleEntity = skeletonPart[weaponPart.muzzleName]
            val muzzleTransform = muzzleEntity[TransformPart::class].transform
            val recoil = VectorUtils.toVector2(muzzleTransform.rotation, weapon.recoil).scl(-1f)
            PhysicsUtils.applyForce(entityManager.all, entity, recoil)
            createBullets(muzzleTransform, weapon)
            weaponPart.reset()
        }
    }

    private fun createBullets(muzzleTransform: Transform, weapon: Weapon) {
        val spreadRange = FloatRange(-weapon.spread / 2, weapon.spread / 2)
        for (i in 0..weapon.numBullets - 1) {
            val angleOffset = spreadRange.random()
            val speed = weapon.speedRange.random()
            entityFactory.createBullet(muzzleTransform, angleOffset, speed, weapon.bulletType)
        }
    }
}