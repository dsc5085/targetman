package dc.targetman.mechanics.weapon

import com.badlogic.gdx.math.Interpolation
import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.level.BulletFactory
import dc.targetman.mechanics.Alliance
import dc.targetman.physics.PhysicsUtils
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.geometry.VectorUtils
import dclib.physics.Transform
import dclib.util.FloatRange
import dclib.util.Maths

class WeaponSystem(private val entityManager: EntityManager, private val bulletFactory: BulletFactory)
: EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val inventoryPart = entity.tryGet(InventoryPart::class)
        val firingPart = entity.tryGet(FiringPart::class)
        if (inventoryPart != null && firingPart != null) {
            val skeletonPart = entity[SkeletonPart::class]
            val equippedWeapon = inventoryPart.equippedWeapon
            skeletonPart.playAnimation("aim", 1)
            if (equippedWeapon != null && hasFiringLimbs(firingPart, skeletonPart)) {
                aim(delta, skeletonPart, firingPart)
                fire(entity)
                equippedWeapon.reloadTimer.tick(delta)
            }
        }
    }

    private fun hasFiringLimbs(firingPart: FiringPart, skeletonPart: SkeletonPart): Boolean {
        val rotatorName = firingPart.rotatorName
        val rotatorLimb = skeletonPart.tryGet(rotatorName)
        return rotatorLimb != null && rotatorLimb.getDescendants().any { it.name == firingPart.muzzleName }
    }

    private fun aim(delta: Float, skeletonPart: SkeletonPart, firingPart: FiringPart) {
        val maxAimSpeed = 960f
        val maxSpeedTime = 0.1f
        val muzzleTransform = skeletonPart[firingPart.muzzleName].transform
        val offsetFromTarget = VectorUtils.offset(muzzleTransform.center, firingPart.targetCoord)
        val flip = if (skeletonPart.flipX) -1f else 1f
        val aimDelta = Maths.degDelta(offsetFromTarget.angle(), muzzleTransform.rotation) * flip
        val aimDeltaRatio = Math.abs(aimDelta / Maths.HALF_DEGREES_MAX)
        val closingAimDelta = Interpolation.exp10Out.apply(0f, maxAimSpeed, aimDeltaRatio)
        val changedAimDirection = Math.signum(firingPart.lastAimDelta) != Math.signum(aimDelta)
        if (changedAimDirection) {
            firingPart.aimTime = 0f
        }
        firingPart.aimTime += delta
        val accleratedAimDelta = Interpolation.exp5In.apply(0f, maxAimSpeed, firingPart.aimTime / maxSpeedTime)
        firingPart.lastAimDelta = Math.min(closingAimDelta, accleratedAimDelta) * Math.signum(aimDelta) * delta
        firingPart.aimAngle += firingPart.lastAimDelta
    }

    private fun fire(entity: Entity) {
        val weapon = entity[InventoryPart::class].equippedWeapon
        val firingPart = entity[FiringPart::class]
        if (weapon != null && weapon.reloadTimer.isElapsed && firingPart.triggered) {
            val skeletonPart = entity[SkeletonPart::class]
            val muzzleTransform = skeletonPart[firingPart.muzzleName].transform
            val recoil = VectorUtils.toVector2(muzzleTransform.rotation, weapon.data.recoil).scl(-1f)
            PhysicsUtils.applyForce(entityManager.getAll(), entity, recoil)
            createBullets(muzzleTransform, weapon, entity.getAttribute(Alliance::class)!!)
            weapon.reloadTimer.reset()
        }
    }

    private fun createBullets(muzzleTransform: Transform, weapon: Weapon, alliance: Alliance) {
        val weaponData = weapon.data
        val spreadRange = FloatRange(-weaponData.spread / 2, weaponData.spread / 2)
        for (i in 0..weaponData.numBullets - 1) {
            val angleOffset = spreadRange.random()
            val speed = weapon.speedRange.random()
            bulletFactory.createBullet(weaponData.bullet, muzzleTransform, angleOffset, speed, alliance)
        }
    }
}