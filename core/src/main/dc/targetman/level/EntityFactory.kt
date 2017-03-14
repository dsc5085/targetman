package dc.targetman.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import dc.targetman.epf.parts.ForcePart
import dc.targetman.epf.parts.ScalePart
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityUtils
import dc.targetman.mechanics.weapon.Bullet
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.physics.collision.Material
import dclib.epf.Entity
import dclib.epf.parts.*
import dclib.geometry.PolygonUtils
import dclib.geometry.VectorUtils
import dclib.geometry.toVector3
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.Transform
import dclib.util.FloatRange
import dclib.util.inv

class EntityFactory(private val factoryTools: FactoryTools) {
    private val entityManager = factoryTools.entityManager
    private val textureCache = factoryTools.textureCache

    // TODO: Use center instead of position
    fun createBullet(bullet: Bullet, muzzleTransform: Transform, angleOffset: Float, speed: Float, alliance: Alliance) {
        val relativeCenter = PolygonUtils.relativeCenter(muzzleTransform.center, bullet.size)
        val position3 = relativeCenter.toVector3()
        val bulletBody = createBody(bullet.regionName, bullet.size, false)
        bulletBody.isBullet = true
        bulletBody.gravityScale = bullet.gravityScale
        val velocity = VectorUtils.toVector2(muzzleTransform.rotation + angleOffset, speed)
        bulletBody.linearVelocity = velocity
        Box2dUtils.setFilter(bulletBody, CollisionCategory.PROJECTILE, CollisionCategory.PROJECTILE.inv())
        val entity = createBaseEntity(bulletBody, position3, bullet.regionName, alliance, Material.METAL)
        // Dampen the y-force to simulate more realistic bullet physics
        val forceScale = Vector2(1f, 0.3f)
        entity.attach(
                AutoRotatePart(),
                CollisionRemovePart(),
                TimedDeathPart(bullet.deathTime),
                CollisionDamagePart(bullet.damage),
                ForcePart(bullet.force, forceScale))
        if (bullet.scaleTime != null) {
            entity.attach(ScalePart(FloatRange(0f, 1f), bullet.scaleTime))
        }
        entityManager.add(entity)
    }

    fun createBloodParticle(size: Float, position: Vector3, velocity: Vector2) {
        val body = createBody("objects/blood", Vector2(size, size), true)
        Box2dUtils.setFilter(body, mask = CollisionCategory.STATIC)
        body.linearVelocity = velocity
        val entity = createBaseEntity(body, position, "objects/blood")
        entity.addAttributes(Material.STICKY)
        entity.attach(CollisionRemovePart(), TimedDeathPart(3f))
        entityManager.add(entity)
    }

    private fun createBaseEntity(body: Body, position: Vector3, regionName: String, vararg attributes: Enum<*>): Entity {
        val transform = Box2dTransform(body, position.z)
        transform.position = Vector2(position.x, position.y)
        val region = textureCache.createHull(regionName).region
        val entity = Entity(TransformPart(transform), SpritePart(region))
        entity.addAttributes(*attributes)
        EntityUtils.filterSameAlliance(entity)
        return entity
    }

    private fun createBody(regionName: String, size: Vector2, sensor: Boolean): Body {
        val hull = textureCache.createHull(regionName, size).hull
        return Box2dUtils.createDynamicBody(factoryTools.world, hull, sensor)
    }
}