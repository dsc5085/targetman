package dc.targetman.mechanics.weapon

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import dc.targetman.audio.SoundManager
import dc.targetman.audio.SoundPlayedEvent
import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.ForcePart
import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.ScalePart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.level.FactoryTools
import dc.targetman.mechanics.ActionKey
import dc.targetman.mechanics.ActionsPart
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityUtils
import dc.targetman.physics.PhysicsUtils
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.physics.collision.Material
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.AutoRotatePart
import dclib.epf.parts.CollisionDamagePart
import dclib.epf.parts.CollisionDestroyPart
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.VectorUtils
import dclib.geometry.toVector3
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.Transform
import dclib.util.FloatRange
import kotlin.experimental.inv

class WeaponSystem(
        private val entityManager: EntityManager,
        private val factoryTools: FactoryTools,
        private val soundManager: SoundManager)
    : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val inventoryPart = entity.tryGet(InventoryPart::class)
        val firingPart = entity.tryGet(FiringPart::class)
        if (inventoryPart != null && firingPart != null) {
            val skeletonPart = entity[SkeletonPart::class]
            val equippedWeapon = inventoryPart.equippedWeapon
            skeletonPart.playAnimation("aim", 1)
            if (equippedWeapon != null && hasFiringLimbs(firingPart, skeletonPart)) {
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

    private fun fire(entity: Entity) {
        val weapon = entity[InventoryPart::class].equippedWeapon
        val firingPart = entity[FiringPart::class]
        val trigger = entity[ActionsPart::class][ActionKey.TRIGGER].doing
        if (weapon != null && weapon.reloadTimer.isElapsed && trigger) {
            val skeletonPart = entity[SkeletonPart::class]
            val muzzleTransform = skeletonPart[firingPart.muzzleName].transform
            val recoil = VectorUtils.toVector2(muzzleTransform.rotation, weapon.data.recoil).scl(-1f)
            PhysicsUtils.applyForce(entityManager.getAll(), entity, recoil)
            createBullets(muzzleTransform, weapon, entity.getAttribute(Alliance::class)!!)
            soundManager.played.notify(SoundPlayedEvent(muzzleTransform.center, /*TODO: Replace hardcoded value*/ 10f, entity))
            weapon.reloadTimer.reset()
        }
    }

    private fun createBullets(muzzleTransform: Transform, weapon: Weapon, alliance: Alliance) {
        val weaponData = weapon.data
        val spreadRange = FloatRange(-weaponData.spread / 2, weaponData.spread / 2)
        for (i in 0 until weaponData.numBullets) {
            val angleOffset = spreadRange.random()
            val speed = weapon.speedRange.random()
            createBullet(weaponData.bullet, muzzleTransform, angleOffset, speed, alliance)
        }
    }

    // TODO: Use center instead of position
    private fun createBullet(
            bullet: Bullet,
            muzzleTransform: Transform,
            angleOffset: Float,
            speed: Float,
            alliance: Alliance
    ) {
        val relativeCenter = PolygonUtils.relativeCenter(muzzleTransform.center, bullet.size)
        val position3 = relativeCenter.toVector3()
        val bulletBody = createBody(bullet.regionName, bullet.size, false)
        bulletBody.isBullet = true
        bulletBody.gravityScale = bullet.gravityScale
        val velocity = VectorUtils.toVector2(muzzleTransform.rotation + angleOffset, speed)
        bulletBody.linearVelocity = velocity
        Box2dUtils.setFilter(bulletBody, CollisionCategory.PROJECTILE, CollisionCategory.PROJECTILE.inv())
        val entity = createBaseEntity(bulletBody, position3, bullet.regionName, alliance, Material.METAL)
        // Dampen the y-force to simulate more realistic bullet collision physics
        entity.attach(
                AutoRotatePart(),
                CollisionDestroyPart(),
                TimedDeathPart(bullet.deathTime),
                CollisionDamagePart(bullet.damage),
                ForcePart(bullet.force))
        if (bullet.scaleTime != null) {
            entity.attach(ScalePart(FloatRange(0f, 1f), bullet.scaleTime))
        }
        entityManager.add(entity)
    }

    private fun createBaseEntity(body: Body, position: Vector3, regionName: String, vararg attributes: Enum<*>): Entity {
        val transform = Box2dTransform(body, position.z)
        transform.position = Vector2(position.x, position.y)
        val region = factoryTools.textureCache.getPolygonRegion(regionName)
        val entity = Entity(TransformPart(transform), SpritePart(region))
        entity.addAttributes(*attributes)
        EntityUtils.filterSameAlliance(entity)
        return entity
    }

    private fun createBody(regionName: String, size: Vector2, sensor: Boolean): Body {
        val hull = factoryTools.textureCache.getHull(regionName, size)
        return Box2dUtils.createDynamicBody(factoryTools.world, hull, sensor)
    }
}