package dc.targetman.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.character.CharacterFactory
import dc.targetman.character.CharacterLoader
import dc.targetman.mechanics.Alliance
import dc.targetman.physics.PhysicsUtils
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.physics.collision.Material
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.CollisionRemovePart
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.geometry.Centrum
import dclib.geometry.PolygonUtils
import dclib.graphics.ConvexHullCache
import dclib.graphics.TextureCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

// TODO: Cleanup
class EntityFactory(
        pixelsPerUnit: Float,
        private val entityManager: EntityManager,
        private val world: World,
        private val textureCache: TextureCache) {
    private val convexHullCache = ConvexHullCache(textureCache)
    private val characterFactory = createCharacterFactory()

    fun createWall(vertices: List<Vector2>?) {
        val entity = Entity()
        val body = PhysicsUtils.createStaticBody(world, PolygonUtils.toFloats(vertices))
        body.userData = entity
        Box2dUtils.setFilter(body, CollisionCategory.STATIC, CollisionCategory.ALL)
        entity.attach(TransformPart(Box2dTransform(0f, body)))
        entity.attribute(Material.METAL)
        entityManager.add(entity)
    }

    fun createMan(position: Vector3, alliance: Alliance): Entity {
        val entity = characterFactory.create("skeletons/man_original.skel", position, alliance)
        entityManager.add(entity)
        return entity
    }

    fun createBullet(centrum: Centrum, angleOffset: Float, speed: Float, type: String) {
        // TODO:
//        val targetAlliance = Alliance.valueOf(type) // TODO: hacky...
//        val size = Vector2(0.08f, 0.08f)
//        val relativeCenter = PolygonUtils.relativeCenter(centrum.position, size)
//        val position3 = Vector3(relativeCenter.x, relativeCenter.y, 0f)
//        val bulletBody = createBody("objects/bullet", size, false)
//        bulletBody.isBullet = true
//        bulletBody.gravityScale = 0.1f
//        val velocity = Vector2(speed, 0f).setAngle(centrum.rotation + angleOffset)
//        bulletBody.linearVelocity = velocity
//        Box2dUtils.setFilter(bulletBody, CollisionCategory.PROJECTILE, CollisionCategory.ALL)
//        val bullet = createBaseEntity(bulletBody, position3, "objects/bullet", targetAlliance.target, Material.METAL)
//        bullet.attach(AutoRotatePart(), TimedDeathPart(3f), CollisionDamagePart(10f), ForcePart(10f))
//        val trailBody = createBody("objects/bullet_trail", Vector2(1.5f, size.y), true)
//        val trail = createBaseEntity(trailBody, Vector3(), "objects/bullet_trail")
//        trail.attach(ScalePart(FloatRange(0f, 1f), 0.2f))
//        entityManager.add(trail)
//        val trailLimb = Limb(trail)
//        val root = Limb(bullet).addJoint(trailLimb, 0.04f, 0.04f, 1.46f, 0.04f, 0f)
//        val limbsPart = SkeletonPart(root)
//        bullet.attach(limbsPart, CollisionRemovePart())
//        entityManager.add(bullet)
    }

    fun createBloodParticle(size: Float, position: Vector3, velocity: Vector2) {
        val body = createBody("objects/blood", Vector2(size, size), true)
        body.linearVelocity = velocity
        val entity = createBaseEntity(body, position, "objects/blood")
        entity.attribute(Material.STICKY)
        entity.attach(CollisionRemovePart(), TimedDeathPart(3f))
        entityManager.add(entity)
    }

    private fun createBaseEntity(body: Body, position: Vector3, regionName: String, vararg attributes: Enum<*>): Entity {
        val entity = Entity()
        body.userData = entity
        entity.attribute(*attributes)
        val transform = Box2dTransform(position.z, body)
        transform.position = Vector2(position.x, position.y)
        val region = textureCache.getPolygonRegion(regionName)
        entity.attach(TransformPart(transform), SpritePart(region))
        setFilterGroup(body, entity.attributes)
        return entity
    }

    private fun createCharacterFactory(): CharacterFactory {
        val characterLoader = CharacterLoader(textureCache)
        return CharacterFactory(characterLoader, convexHullCache, world)
    }

    private fun createBody(regionName: String, size: Vector2, sensor: Boolean): Body {
        val hull = convexHullCache.create(regionName, size).hull
        return PhysicsUtils.createDynamicBody(world, hull, sensor)
    }

    private fun setFilterGroup(body: Body, attributes: Set<Enum<*>>) {
        val alliance = attributes.filterIsInstance(Alliance::class.java).firstOrNull()
        if (alliance != null) {
            Box2dUtils.setFilter(body, group = (-alliance.ordinal).toShort())
        }
    }
}