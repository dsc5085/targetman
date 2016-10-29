package dc.targetman.level;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import dc.targetman.epf.parts.AiPart;
import dc.targetman.epf.parts.ForcePart;
import dc.targetman.epf.parts.MovementPart;
import dc.targetman.epf.parts.ScalePart;
import dc.targetman.epf.parts.VitalLimbsPart;
import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.mechanics.Alliance;
import dc.targetman.mechanics.Weapon;
import dc.targetman.physics.collision.Material;
import dc.targetman.physics.limb.WalkAnimation;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.AutoRotatePart;
import dclib.epf.parts.CollisionDamagePart;
import dclib.epf.parts.CollisionRemovePart;
import dclib.epf.parts.HealthPart;
import dclib.epf.parts.LimbAnimationsPart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.SpritePart;
import dclib.epf.parts.TimedDeathPart;
import dclib.epf.parts.TransformPart;
import dclib.geometry.Centrum;
import dclib.geometry.PolygonUtils;
import dclib.graphics.ConvexHullCache;
import dclib.graphics.TextureCache;
import dclib.physics.Box2dTransform;
import dclib.physics.Transform;
import dclib.physics.limb.Joint;
import dclib.physics.limb.Limb;
import dclib.physics.limb.LimbAnimation;
import dclib.physics.limb.Rotator;
import dclib.util.FloatRange;
import net.dermetfan.gdx.math.BayazitDecomposer;

// TODO: Cleanup
public final class EntityFactory {

	private final EntityManager entityManager;
	private final World world;
	private final TextureCache textureCache;
	private final ConvexHullCache convexHullCache;

	public EntityFactory(final EntityManager entityManager, final World world, final TextureCache textureCache) {
		this.entityManager = entityManager;
		this.world = world;
		this.textureCache = textureCache;
		convexHullCache = new ConvexHullCache(textureCache);
	}

	public final void createWall(final Vector2 size, final Vector3 position) {
		Entity entity = new Entity();
		Polygon polygon = convexHullCache.create("objects/white", size);
		polygon.setPosition(position.x,  position.y);

		BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
		Body body = world.createBody(def);
		body.setUserData(entity);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(size.x / 2, size.y / 2);
		body.createFixture(shape, 0);
		shape.dispose();
		body.setTransform(position.x + size.x / 2, position.y + size.y / 2, 0);

		entity.attach(new TransformPart(new Box2dTransform(position.z, body)));
		entity.attribute(Material.METAL);
		entityManager.add(entity);
	}

	public final Entity createStickman(final Vector3 position, final Alliance alliance) {
		Limb leftForearm = new Limb();
		Limb leftBicep = new Limb().addJoint(leftForearm, 0.4f, 0.05f, 0, 0.05f, 45);
		Limb gun = new Limb();
		Limb rightForearm = new Limb().addJoint(gun, 0.4f, 0.05f, 0.1f, 0.05f, 0);
		Limb rightBicep = new Limb().addJoint(rightForearm, 0.4f, 0.05f, 0, 0.05f, 45);
		Limb head = new Limb();
		Joint rightBicepJoint = new Joint(rightBicep, new Vector2(0.8f, 0.05f), new Vector2(0, 0.05f), -135);
		Limb torso = new Limb()
				.addJoint(leftBicep, 0.8f, 0.05f, 0, 0.05f, -225)
				.addJoint(rightBicepJoint)
				.addJoint(head, 1, 0.05f, 0.05f, 0.25f, 0);
		Limb leftLeg = new Limb();
		Limb rightLeg = new Limb();
		Limb[] zOrder = new Limb[] { leftForearm, leftBicep, leftLeg, torso, head, rightLeg, rightBicep, rightForearm };
		createLimbEntity(leftForearm, zOrder, new Vector2(0.4f, 0.1f), position, "objects/limb", 100, alliance, Material.FLESH);
		createLimbEntity(leftBicep, zOrder, new Vector2(0.4f, 0.1f), position, "objects/limb", 100, alliance, Material.FLESH);
		createLimbEntity(gun, zOrder, new Vector2(0.4f, 0.3f), position, "objects/gun", 500, Material.METAL);
		createLimbEntity(rightForearm,  zOrder, new Vector2(0.4f, 0.1f), position, "objects/limb", 100, alliance, Material.FLESH);
		createLimbEntity(rightBicep,  zOrder, new Vector2(0.4f, 0.1f), position, "objects/limb", 100, alliance, Material.FLESH);
		createLimbEntity(head,  zOrder, new Vector2(0.5f, 0.5f), position, "objects/head", 100, alliance, Material.FLESH);
		createLimbEntity(torso, zOrder, new Vector2(1, 0.1f), position, "objects/limb", 200, alliance, Material.FLESH);
		createLimbEntity(leftLeg, zOrder, new Vector2(1, 0.1f), position, "objects/limb", 100, alliance, Material.FLESH);
		createLimbEntity(rightLeg, zOrder, new Vector2(1, 0.1f), position, "objects/limb", 100, alliance, Material.FLESH);
		Joint leftLegJoint = new Joint(leftLeg, new Vector2(), new Vector2(0, 0.05f), -110);
		Joint rightLegJoint = new Joint(rightLeg, new Vector2(), new Vector2(0, 0.05f), -70);
		Entity entity = new Entity();
		entity.attribute(alliance);

		float halfHeight = 1.05f;
		float halfWidth = 0.3f;
		BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		Body body = world.createBody(def);
		CircleShape baseShape = new CircleShape();
		baseShape.getPosition();
		baseShape.setRadius(halfWidth);
		baseShape.setPosition(new Vector2(0, -halfHeight));
		body.createFixture(baseShape, 0).setFriction(50);
		baseShape.dispose();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(halfWidth, halfHeight);
		body.createFixture(shape, 1).setFriction(0);
		shape.dispose();
		body.setBullet(true);
		body.setFixedRotation(true);
		body.setTransform(position.x, position.y, 0);
		body.setUserData(entity);

		Limb root = new Limb()
		.addJoint(torso, 0, 0, 0.05f, 0.05f, 90)
		.addJoint(leftLegJoint)
		.addJoint(rightLegJoint);
		Transform transform = new Box2dTransform(position.z, body);
		entity.attach(new TransformPart(transform));
		LimbAnimation walkAnimation = new WalkAnimation(leftLegJoint, rightLegJoint, new FloatRange(-110, -70));
		Map<String, LimbAnimation> animations = new HashMap<String, LimbAnimation>();
		animations.put("walk", walkAnimation);
		Rotator rotator = new Rotator(rightBicepJoint, new FloatRange(-180, -45), 135);
		Centrum weaponCentrum = new Centrum(gun.getTransform(), new Vector2(0.4f, 0.25f));
		Weapon weapon = new Weapon(0.5f, 1, 35, 14, 16, alliance.getTarget().name());
		entity.attach(
				new LimbAnimationsPart(animations),
				new MovementPart(10, 12, leftLeg, rightLeg),
				new WeaponPart(weaponCentrum, weapon, rotator),
				new LimbsPart(root, leftLeg, rightLeg),
				new VitalLimbsPart(head, torso));
		if (alliance == Alliance.ENEMY){
			entity.attach(new AiPart());
		}

		entityManager.add(entity);
		return entity;
	}

	public final void createBullet(final Centrum centrum, final float angleOffset, final float speed, final String type) {
		Alliance targetAlliance = Alliance.valueOf(type); // TODO: hacky...
		Vector2 size = new Vector2(0.08f, 0.08f);
		Vector2 relativeCenter = PolygonUtils.relativeCenter(centrum.getPosition(), size);
		Vector3 position3 = new Vector3(relativeCenter.x, relativeCenter.y, 0);
		Body bulletBody = createBody("objects/bullet", size, false);
		bulletBody.setBullet(true);
		bulletBody.setGravityScale(0.1f);
		Vector2 velocity = new Vector2(speed, 0).setAngle(centrum.getRotation() + angleOffset);
		bulletBody.setLinearVelocity(velocity);
		Entity bullet = createBaseEntity(bulletBody, position3, "objects/bullet", new Enum<?>[] { targetAlliance.getTarget(), Material.METAL });
		bullet.attach(new AutoRotatePart(), new TimedDeathPart(3), new CollisionDamagePart(10), new ForcePart(5));
		Body trailBody = createBody("objects/bullet_trail", new Vector2(1.5f, size.y), true);
		Entity trail = createBaseEntity(trailBody, new Vector3(), "objects/bullet_trail");
		trail.attach(new ScalePart(new FloatRange(0, 1), 0.2f));
		entityManager.add(trail);
		Limb trailLimb = new Limb(trail.get(TransformPart.class).getTransform());
		Transform transform = bullet.get(TransformPart.class).getTransform();
		Limb root = new Limb(transform).addJoint(trailLimb, 0.04f, 0.04f, 1.46f, 0.04f, 0);
		LimbsPart limbsPart = new LimbsPart(root, root);
		bullet.attach(limbsPart, new CollisionRemovePart());
		entityManager.add(bullet);
	}

	public final void createBloodParticle(final float size, final Vector3 position, final Vector2 velocity) {
		Body body = createBody("objects/blood", new Vector2(size, size), true);
		body.setLinearVelocity(velocity);
		Entity entity = createBaseEntity(body, position, "objects/blood");
		entity.attribute(Material.STICKY);
		entity.attach(new CollisionRemovePart(), new TimedDeathPart(3));
		entityManager.add(entity);
	}

	private final void createLimbEntity(final Limb limb, final Limb[] zOrder, final Vector2 size, final Vector3 position, final String regionName, final float health, final Enum<?>...attributes) {
		float z = position.z + ArrayUtils.indexOf(zOrder, limb) * MathUtils.FLOAT_ROUNDING_ERROR;
		Body body = createBody(regionName, size, true);
		body.setGravityScale(0);
		Entity entity = createBaseEntity(body, new Vector3(position.x, position.y, z), regionName, attributes);
		entity.attach(new HealthPart(health));
		limb.setTransform(entity.get(TransformPart.class).getTransform());
		entityManager.add(entity);
	}

	private final Entity createBaseEntity(final Body body, final Vector3 position, final String regionName) {
		return createBaseEntity(body, position, regionName, new Enum<?>[0]);
	}

	private final Entity createBaseEntity(final Body body, final Vector3 position, final String regionName, final Enum<?>[] collisionGroups) {
		Entity entity = new Entity();
		entity.attribute(collisionGroups);
		body.setUserData(entity);
		Transform transform = new Box2dTransform(position.z, body);
		transform.setPosition(new Vector2(position.x, position.y));
		PolygonRegion region = textureCache.getPolygonRegion(regionName);
		entity.attach(new TransformPart(transform), new SpritePart(region));
		return entity;
	}

	private final Body createBody(final String regionName, final Vector2 size, final boolean sensor) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		Body body = world.createBody(bodyDef);
		float[] vertices = convexHullCache.create(regionName, size).getVertices();
		Array<Vector2> vertexVectors = new Array<Vector2>(PolygonUtils.toVectors(vertices));
		for (Array<Vector2> partition : BayazitDecomposer.convexPartition(vertexVectors)) {
			PolygonShape shape = new PolygonShape();
			Vector2[] partitionVectors = partition.toArray(Vector2.class);
			shape.set(PolygonUtils.toFloats(partitionVectors));
			body.createFixture(shape, 1).setSensor(sensor);
			shape.dispose();
		}
		return body;
	}

}
