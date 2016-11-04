package dc.targetman.level

import java.util.HashMap
import org.apache.commons.lang3.ArrayUtils
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.Filter
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.ForcePart
import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.ScalePart
import dc.targetman.epf.parts.VitalLimbsPart
import dc.targetman.epf.parts.WeaponPart
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.CollisionCategory
import dc.targetman.mechanics.Weapon
import dc.targetman.physics.collision.Material
import dc.targetman.physics.limb.WalkAnimation
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.AutoRotatePart
import dclib.epf.parts.CollisionDamagePart
import dclib.epf.parts.CollisionRemovePart
import dclib.epf.parts.HealthPart
import dclib.epf.parts.LimbAnimationsPart
import dclib.epf.parts.LimbsPart
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.geometry.Centrum
import dclib.geometry.PolygonUtils
import dclib.graphics.ConvexHullCache
import dclib.graphics.TextureCache
import dclib.physics.Box2dTransform
import dclib.physics.Transform
import dclib.physics.limb.Joint
import dclib.physics.limb.Limb
import dclib.physics.limb.LimbAnimation
import dclib.physics.limb.Rotator
import dclib.util.FloatRange
import net.dermetfan.gdx.math.BayazitDecomposer

// TODO: Cleanup
class EntityFactory(entityManager: EntityManager, world: World, textureCache: TextureCache) {
	private val entityManager: EntityManager = entityManager
	private val world: World = world
	private val textureCache: TextureCache = textureCache
	private val convexHullCache: ConvexHullCache

	init {
		convexHullCache = ConvexHullCache(textureCache)
	}

	fun createWall(vertices: List<Vector2>?) {
		val entity = Entity()
		val def = BodyDef()
		def.type = BodyType.StaticBody
		val body = createBody(def, PolygonUtils.toFloats(vertices), false)
		body.setUserData(entity)
		entity.attach(TransformPart(Box2dTransform(0f, body)))
		entity.attribute(Material.METAL)
		entityManager.add(entity)
	}

	fun createStickman(position: Vector3, alliance: Alliance): Entity {
		val leftForearm = Limb()
		val leftBicep = Limb().addJoint(leftForearm, 0.4f, 0.05f, 0f, 0.05f, 45f)
		val gun = Limb()
		val rightForearm = Limb().addJoint(gun, 0.4f, 0.05f, 0.1f, 0.05f, 0f)
		val rightBicep = Limb().addJoint(rightForearm, 0.4f, 0.05f, 0f, 0.05f, 45f)
		val head = Limb()
		val rightBicepJoint = Joint(rightBicep, Vector2(0.8f, 0.05f), Vector2(0f, 0.05f), -135f)
		val torso = Limb()
				.addJoint(leftBicep, 0.8f, 0.05f, 0f, 0.05f, -225f)
				.addJoint(rightBicepJoint)
				.addJoint(head, 1f, 0.05f, 0.05f, 0.25f, 0f)
		val leftLeg = Limb()
		val rightLeg = Limb()
		val zOrder = arrayOf<Limb>(leftForearm, leftBicep, leftLeg, torso, head, rightLeg, rightBicep, rightForearm)
		createLimbEntity(leftForearm, zOrder, Vector2(0.4f, 0.1f), position, "objects/limb", 100f, alliance, Material.FLESH)
		createLimbEntity(leftBicep, zOrder, Vector2(0.4f, 0.1f), position, "objects/limb", 100f, alliance, Material.FLESH)
		createLimbEntity(gun, zOrder, Vector2(0.4f, 0.3f), position, "objects/gun", 500f, Material.METAL)
		createLimbEntity(rightForearm, zOrder, Vector2(0.4f, 0.1f), position, "objects/limb", 100f, alliance, Material.FLESH)
		createLimbEntity(rightBicep, zOrder, Vector2(0.4f, 0.1f), position, "objects/limb", 100f, alliance, Material.FLESH)
		createLimbEntity(head, zOrder, Vector2(0.5f, 0.5f), position, "objects/head", 100f, alliance, Material.FLESH)
		createLimbEntity(torso, zOrder, Vector2(1f, 0.1f), position, "objects/limb", 200f, alliance, Material.FLESH)
		createLimbEntity(leftLeg, zOrder, Vector2(1f, 0.1f), position, "objects/limb", 100f, alliance, Material.FLESH)
		createLimbEntity(rightLeg, zOrder, Vector2(1f, 0.1f), position, "objects/limb", 100f, alliance, Material.FLESH)
		val leftLegJoint = Joint(leftLeg, Vector2(), Vector2(0f, 0.05f), -110f)
		val rightLegJoint = Joint(rightLeg, Vector2(), Vector2(0f, 0.05f), -70f)
		val entity = Entity()
		entity.attribute(alliance)
		val halfHeight = 1.05f
		val halfWidth = 0.3f
		val def = BodyDef()
		def.type = BodyType.DynamicBody
		val body = world.createBody(def)
		val baseShape = CircleShape()
		baseShape.radius = halfWidth
		baseShape.position = Vector2(0f, -halfHeight)
		body.createFixture(baseShape, 0f).setFriction(50f)
		baseShape.dispose()
		val shape = PolygonShape()
		shape.setAsBox(halfWidth, halfHeight)
		body.createFixture(shape, 1f).setFriction(0f)
		shape.dispose()
		body.setBullet(true)
		body.setFixedRotation(true)
		body.setTransform(position.x, position.y, 0f)
		body.setUserData(entity)
		setFilter(body, CollisionCategory.BOUNDS, CollisionCategory.PROJECTILE.toInt().inv().toShort() /* TODO: create a custom method for short inv() */)
		val root = Limb().addJoint(torso, 0f, 0f, 0.05f, 0.05f, 90f).addJoint(leftLegJoint).addJoint(rightLegJoint)
		val transform = Box2dTransform(position.z, body)
		entity.attach(TransformPart(transform))
		val walkAnimation = WalkAnimation(leftLegJoint, rightLegJoint, FloatRange(-110f, -70f))
		val animations = HashMap<String, LimbAnimation>()
		animations.put("walk", walkAnimation)
		val rotator = Rotator(rightBicepJoint, FloatRange(-180f, -45f), 135f)
		val weaponCentrum = Centrum(gun.getTransform(), Vector2(0.4f, 0.25f))
		val weapon = Weapon(0.03f, 1, 35f, 14f, 16f, alliance.target.name)
		entity.attach(
				LimbAnimationsPart(animations),
				MovementPart(10f, 12f, leftLeg, rightLeg),
				WeaponPart(weaponCentrum, weapon, rotator),
				LimbsPart(root),
				VitalLimbsPart(head, torso))
		if (alliance === Alliance.ENEMY) {
			entity.attach(AiPart())
		}
		entityManager.add(entity)
		return entity
	}

	fun createBullet(centrum: Centrum, angleOffset: Float, speed: Float, type: String) {
		val targetAlliance = Alliance.valueOf(type) // TODO: hacky...
		val size = Vector2(0.08f, 0.08f)
		val relativeCenter = PolygonUtils.relativeCenter(centrum.getPosition(), size)
		val position3 = Vector3(relativeCenter.x, relativeCenter.y, 0f)
		val bulletBody = createBody("objects/bullet", size, false)
		bulletBody.setBullet(true)
		bulletBody.gravityScale = 0.1f
		val velocity = Vector2(speed, 0f).setAngle(centrum.getRotation() + angleOffset)
		bulletBody.linearVelocity = velocity
		setFilter(bulletBody, CollisionCategory.PROJECTILE, CollisionCategory.ALL)
		val bullet = createBaseEntity(bulletBody, position3, "objects/bullet", arrayOf<Enum<*>>(targetAlliance.target, Material.METAL))
		bullet.attach(AutoRotatePart(), TimedDeathPart(3f), CollisionDamagePart(10f), ForcePart(5f))
		val trailBody = createBody("objects/bullet_trail", Vector2(1.5f, size.y), true)
		val trail = createBaseEntity(trailBody, Vector3(), "objects/bullet_trail")
		trail.attach(ScalePart(FloatRange(0f, 1f), 0.2f))
		entityManager.add(trail)
		val trailLimb = Limb(trail[TransformPart::class.java].transform)
		val transform = bullet[TransformPart::class.java].transform
		val root = Limb(transform).addJoint(trailLimb, 0.04f, 0.04f, 1.46f, 0.04f, 0f)
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

	private fun createLimbEntity(limb: Limb, zOrder: Array<Limb>, size: Vector2, position: Vector3, regionName: String, health: Float, vararg attributes: Enum<*>) {
		val z = position.z + ArrayUtils.indexOf(zOrder, limb) * MathUtils.FLOAT_ROUNDING_ERROR
		val body = createBody(regionName, size, true)
		body.gravityScale = 0f
		val entity = createBaseEntity(body, Vector3(position.x, position.y, z), regionName, attributes)
		entity.attach(HealthPart(health))
		limb.setTransform(entity[TransformPart::class.java].transform)
		entityManager.add(entity)
	}

	private fun createBaseEntity(body: Body, position: Vector3, regionName: String, attributes: Array<out Enum<*>> = arrayOf<Enum<*>>()): Entity {
		val entity = Entity()
		// TODO: need to figure out how to pass in a Enum<*> array as a vararg instead of looping through each value
		for (attribute in attributes) {
			entity.attribute(attribute)
		}
		body.setUserData(entity)
		val transform = Box2dTransform(position.z, body)
		transform.setPosition(Vector2(position.x, position.y))
		val region = textureCache.getPolygonRegion(regionName)
		entity.attach(TransformPart(transform), SpritePart(region))
		return entity
	}

	private fun createBody(regionName: String, size: Vector2, sensor: Boolean): Body {
		val bodyDef = BodyDef()
		bodyDef.type = BodyType.DynamicBody
		val vertices = convexHullCache.create(regionName, size).getVertices()
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
			fixture.setSensor(sensor)
			shape.dispose()
		}
		return body
	}

	private fun setFilter(body: Body, category: Short, mask: Short) {
		val filter = Filter()
		filter.categoryBits = category
		filter.maskBits = mask
		for (fixture in body.fixtureList) {
			fixture.filterData = filter
		}
	}
}