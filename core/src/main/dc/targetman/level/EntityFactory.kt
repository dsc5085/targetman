package dc.targetman.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.epf.parts.ForcePart
import dc.targetman.epf.parts.ScalePart
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityUtils
import dc.targetman.mechanics.weapon.Bullet
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.physics.collision.Material
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.*
import dclib.geometry.PolygonUtils
import dclib.geometry.VectorUtils
import dclib.geometry.toVector3
import dclib.graphics.ConvexHullCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.Transform
import dclib.util.FloatRange
import dclib.util.inv

class EntityFactory(
        private val entityManager: EntityManager,
        private val world: World,
        private val convexHullCache: ConvexHullCache
) {
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
        entity.attach(
                AutoRotatePart(),
                CollisionRemovePart(),
                TimedDeathPart(bullet.deathTime),
                CollisionDamagePart(bullet.damage),
                ForcePart(bullet.force))
        if (bullet.scaleTime != null) {
            entity.attach(ScalePart(FloatRange(0f, 1f), bullet.scaleTime))
        }
        entityManager.add(entity)
    }

    fun createBloodParticle(size: Float, position: Vector3, velocity: Vector2) {
        val body = createBody("objects/blood", Vector2(size, size), true)
        Box2dUtils.setFilter(body, CollisionCategory.ALL, CollisionCategory.STATIC)
        body.linearVelocity = velocity
        val entity = createBaseEntity(body, position, "objects/blood")
        entity.addAttributes(Material.STICKY)
        entity.attach(CollisionRemovePart(), TimedDeathPart(3f))
        entityManager.add(entity)
    }

    private fun createBaseEntity(body: Body, position: Vector3, regionName: String, vararg attributes: Enum<*>): Entity {
        val entity = Entity()
        entity.addAttributes(*attributes)
        val transform = Box2dTransform(body, position.z)
        transform.position = Vector2(position.x, position.y)
        val region = convexHullCache.create(regionName).region
        entity.attach(TransformPart(transform), SpritePart(region))
        EntityUtils.filterSameAlliance(entity)
        return entity
    }

    private fun createBody(regionName: String, size: Vector2, sensor: Boolean): Body {
        val hull = convexHullCache.create(regionName, size).hull
        return Box2dUtils.createDynamicBody(world, hull, sensor)
    }
}