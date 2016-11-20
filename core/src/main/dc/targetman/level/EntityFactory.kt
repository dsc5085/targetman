package dc.targetman.level

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import dc.targetman.ai.AiProfile
import dc.targetman.epf.parts.*
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.DeathForm
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.physics.collision.Material
import dc.targetman.physics.limb.WalkAnimation
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.*
import dclib.geometry.Centrum
import dclib.geometry.PolygonUtils
import dclib.graphics.ConvexHullCache
import dclib.graphics.TextureCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.limb.Joint
import dclib.physics.limb.Limb
import dclib.physics.limb.LimbAnimation
import dclib.physics.limb.Rotator
import dclib.util.FloatRange
import net.dermetfan.gdx.math.BayazitDecomposer
import java.util.*

// TODO: Cleanup
class EntityFactory(private val entityManager: EntityManager,
                    private val world: World,
                    private val textureCache: TextureCache) {
    private val convexHullCache = ConvexHullCache(textureCache)

    fun createWall(vertices: List<Vector2>?) {
        val entity = Entity()
        val def = BodyDef()
        def.type = BodyType.StaticBody
        val body = createBody(def, PolygonUtils.toFloats(vertices), false)
        body.userData = entity
        Box2dUtils.setFilter(body, CollisionCategory.STATIC, CollisionCategory.ALL)
        entity.attach(TransformPart(Box2dTransform(0f, body)))
        entity.attribute(Material.METAL)
        entityManager.add(entity)
    }

    fun createStickman(position: Vector3, alliance: Alliance): Entity {
        val root = createLimb(Vector2(Box2dUtils.ROUNDING_ERROR, Box2dUtils.ROUNDING_ERROR), position, "objects/transparent", 50f, alliance, Material.FLESH)
        val leftForearm = createLimb(Vector2(0.4f, 0.1f), position, "objects/limb", 50f, alliance, Material.FLESH)
        val leftBicep = createLimb(Vector2(0.4f, 0.1f), position, "objects/limb", 50f, alliance, Material.FLESH)
                .addJoint(leftForearm, 0.4f, 0.05f, 0f, 0.05f, 45f)
        val gun = createLimb(Vector2(0.4f, 0.3f), position, "objects/gun", 250f, alliance)
        val rightForearm = createLimb(Vector2(0.4f, 0.1f), position, "objects/limb", 50f, alliance, Material.FLESH)
                .addJoint(gun, 0.4f, 0.05f, 0.1f, 0.05f, 0f)
        val rightBicep = createLimb(Vector2(0.4f, 0.1f), position, "objects/limb", 50f, alliance, Material.FLESH)
                .addJoint(rightForearm, 0.4f, 0.05f, 0f, 0.05f, 45f)
        val rightBicepJoint = Joint(rightBicep, Vector2(0.8f, 0.05f), Vector2(0f, 0.05f), -135f)
        val head = createLimb(Vector2(0.5f, 0.5f), position, "objects/head", 50f, alliance, Material.FLESH)
        val torso = createLimb(Vector2(1f, 0.1f), position, "objects/limb", 100f, alliance, Material.FLESH)
                .addJoint(leftBicep, 0.8f, 0.05f, 0f, 0.05f, -225f)
                .addJoint(rightBicepJoint)
                .addJoint(head, 1f, 0.05f, 0.05f, 0.25f, 0f)
        val leftLeg = createLimb(Vector2(1f, 0.1f), position, "objects/limb", 75f, alliance, Material.FLESH)
        val leftLegJoint = Joint(leftLeg, Vector2(), Vector2(0f, 0.05f), -110f)
        val rightLeg = createLimb(Vector2(1f, 0.1f), position, "objects/limb", 75f, alliance, Material.FLESH)
        val rightLegJoint = Joint(rightLeg, Vector2(), Vector2(0f, 0.05f), -70f)
        val entity = Entity()
        entity.attribute(alliance)
        val halfHeight = 1.05f
        val halfWidth = 0.3f
        val def = BodyDef()
        def.type = BodyType.DynamicBody
        val body = world.createBody(def)
        body.userData = entity
        val baseShape = CircleShape()
        baseShape.radius = halfWidth
        baseShape.position = Vector2(0f, -halfHeight)
        val baseFixture = body.createFixture(baseShape, 0f)
        baseFixture.friction = 0.1f
        baseShape.dispose()
        val shape = PolygonShape()
        shape.setAsBox(halfWidth, halfHeight)
        val bodyFixture = body.createFixture(shape, 1f)
        bodyFixture.friction = 0f
        shape.dispose()
        body.isBullet = true
        body.isFixedRotation = true
        body.setTransform(position.x, position.y, 0f)
        Box2dUtils.setFilter(body, CollisionCategory.BOUNDS, CollisionCategory.PROJECTILE.toInt().inv().toShort() /* TODO: create a custom method for short inv() */)
        root.addJoint(torso, 0f, 0f, 0.05f, 0.05f, 90f).addJoint(leftLegJoint).addJoint(rightLegJoint)
        val transform = Box2dTransform(position.z, body)
        entity.attach(TransformPart(transform))
        val walkAnimation = WalkAnimation(leftLegJoint, rightLegJoint, FloatRange(-110f, -70f))
        val animations = HashMap<String, LimbAnimation>()
        animations.put("walk", walkAnimation)
        val rotator = Rotator(rightBicepJoint, FloatRange(-180f, -45f), 135f)
        val weaponCentrum = Centrum(gun.transform, Vector2(0.4f, 0.25f))
        val weapon = Weapon(0.1f, 1, 35f, 28f, 32f, 0f, alliance.target.name)
        entity.attach(
                LimbAnimationsPart(animations),
                MovementPart(8f, 9f, leftLeg, rightLeg),
                WeaponPart(weaponCentrum, weapon, rotator),
                LimbsPart(root),
                VitalLimbsPart(head, torso))
        if (alliance === Alliance.ENEMY) {
            entity.attach(AiPart(AiProfile(0f, 1.5f)))
        }
        entityManager.add(entity)
        return entity
    }

    fun createBullet(centrum: Centrum, angleOffset: Float, speed: Float, type: String) {
        val targetAlliance = Alliance.valueOf(type) // TODO: hacky...
        val size = Vector2(0.08f, 0.08f)
        val relativeCenter = PolygonUtils.relativeCenter(centrum.position, size)
        val position3 = Vector3(relativeCenter.x, relativeCenter.y, 0f)
        val bulletBody = createBody("objects/bullet", size, false)
        bulletBody.isBullet = true
        bulletBody.gravityScale = 0.1f
        val velocity = Vector2(speed, 0f).setAngle(centrum.rotation + angleOffset)
        bulletBody.linearVelocity = velocity
        Box2dUtils.setFilter(bulletBody, CollisionCategory.PROJECTILE, CollisionCategory.ALL)
        val bullet = createBaseEntity(bulletBody, position3, "objects/bullet", targetAlliance.target, Material.METAL)
        bullet.attach(AutoRotatePart(), TimedDeathPart(3f), CollisionDamagePart(10f), ForcePart(10f))
        val trailBody = createBody("objects/bullet_trail", Vector2(1.5f, size.y), true)
        val trail = createBaseEntity(trailBody, Vector3(), "objects/bullet_trail")
        trail.attach(ScalePart(FloatRange(0f, 1f), 0.2f))
        entityManager.add(trail)
        val trailLimb = Limb(trail)
        val root = Limb(bullet).addJoint(trailLimb, 0.04f, 0.04f, 1.46f, 0.04f, 0f)
        val limbsPart = LimbsPart(root)
        bullet.attach(limbsPart, CollisionRemovePart())
        entityManager.add(bullet)
    }

    fun createBloodParticle(size: Float, position: Vector3, velocity: Vector2) {
        val body = createBody("objects/blood", Vector2(size, size), true)
        body.linearVelocity = velocity
        val entity = createBaseEntity(body, position, "objects/blood")
        entity.attribute(Material.STICKY)
        entity.attach(CollisionRemovePart(), TimedDeathPart(3f))
        entityManager.add(entity)
    }

    private fun createLimb(size: Vector2, position: Vector3, regionName: String, health: Float, vararg attributes: Enum<*>): Limb {
        val body = createBody(regionName, size, true)
        body.gravityScale = 0f
        val entity = createBaseEntity(body, Vector3(position.x, position.y, 0f), regionName, *attributes, DeathForm.CORPSE)
        entity.attach(HealthPart(health))
        entityManager.add(entity)
        return Limb(entity)
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

    private fun createBody(regionName: String, size: Vector2, sensor: Boolean): Body {
        val bodyDef = BodyDef()
        bodyDef.type = BodyType.DynamicBody
        val vertices = convexHullCache.create(regionName, size).vertices
        return createBody(bodyDef, vertices, sensor)
    }

    private fun createBody(bodyDef: BodyDef, vertices: FloatArray, sensor: Boolean): Body {
        val body = world.createBody(bodyDef)
        val vertexVectors = com.badlogic.gdx.utils.Array<Vector2>(Iterables.toArray(PolygonUtils.toVectors(vertices), Vector2::class.java))
        for (partition in BayazitDecomposer.convexPartition(vertexVectors)) {
            val shape = PolygonShape()
            val partitionVectors = Lists.newArrayList(partition)
            shape.set(PolygonUtils.toFloats(partitionVectors))
            val fixture = body.createFixture(shape, 1f)
            fixture.isSensor = sensor
            shape.dispose()
        }
        return body
    }

    private fun setFilterGroup(body: Body, attributes: Set<Enum<*>>) {
        val alliance = attributes.filterIsInstance(Alliance::class.java).firstOrNull()
        if (alliance != null) {
            Box2dUtils.setFilter(body, group = (-alliance.ordinal).toShort())
        }
    }
}