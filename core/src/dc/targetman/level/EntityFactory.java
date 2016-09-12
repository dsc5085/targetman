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
import dc.targetman.epf.parts.CollisionRemovePart;
import dc.targetman.epf.parts.ForcePart;
import dc.targetman.epf.parts.MovementPart;
import dc.targetman.epf.parts.ScalePart;
import dc.targetman.epf.parts.StickyPart;
import dc.targetman.epf.parts.VitalLimbsPart;
import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.level.models.Alliance;
import dc.targetman.level.models.CollisionType;
import dc.targetman.limb.WalkAnimation;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.AutoRotatePart;
import dclib.epf.parts.BodyPart;
import dclib.epf.parts.CollisionDamagePart;
import dclib.epf.parts.CollisionPart;
import dclib.epf.parts.DrawablePart;
import dclib.epf.parts.HealthPart;
import dclib.epf.parts.LimbAnimationsPart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.TimedDeathPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.geometry.Centrum;
import dclib.geometry.PolygonUtils;
import dclib.geometry.Transform;
import dclib.geometry.VertexUtils;
import dclib.graphics.ConvexHullCache;
import dclib.graphics.TextureCache;
import dclib.limb.Joint;
import dclib.limb.Limb;
import dclib.limb.LimbAnimation;
import dclib.limb.Rotator;
import dclib.physics.Box2dTransform;
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
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(size.x / 2, size.y / 2);
		body.createFixture(shape, 0);
		shape.dispose();
		body.setTransform(position.x + size.x / 2, position.y + size.y / 2, 0);

		// TODO: Create convenience method to shorten new TransformPart(new DefaultTransform(position.z, polygon))
		entity.attach(new TransformPart(new Box2dTransform(position.z, body)), new CollisionPart(CollisionType.METAL));
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
		float startZ = position.z;
		createLimbEntity(leftForearm, startZ, zOrder, new Vector2(0.4f, 0.1f), "objects/limb", 100, alliance);
		createLimbEntity(leftBicep, startZ, zOrder, new Vector2(0.4f, 0.1f), "objects/limb", 100, alliance);
		createLimbEntity(gun, startZ, zOrder, new Vector2(0.4f, 0.3f), "objects/gun", 500, alliance, CollisionType.METAL);
		createLimbEntity(rightForearm, startZ, zOrder, new Vector2(0.4f, 0.1f), "objects/limb", 100, alliance);
		createLimbEntity(rightBicep, startZ, zOrder, new Vector2(0.4f, 0.1f), "objects/limb", 100, alliance);
		createLimbEntity(head, startZ, zOrder, new Vector2(0.5f, 0.5f), "objects/head", 100, alliance);
		createLimbEntity(torso, startZ, zOrder, new Vector2(1, 0.1f), "objects/limb", 200, alliance);
		createLimbEntity(leftLeg, startZ, zOrder, new Vector2(1, 0.1f), "objects/limb", 100, alliance);
		createLimbEntity(rightLeg, startZ, zOrder, new Vector2(1, 0.1f), "objects/limb", 100, alliance);
		Joint leftLegJoint = new Joint(leftLeg, new Vector2(), new Vector2(0, 0.05f), -110);
		Joint rightLegJoint = new Joint(rightLeg, new Vector2(), new Vector2(0, 0.05f), -70);
		Entity entity = new Entity();

		float halfHeight = 1.05f;
		float halfWidth = 0.3f;
		BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		Body body = world.createBody(def);
		CircleShape baseShape = new CircleShape();
		baseShape.getPosition();
		baseShape.setRadius(halfWidth);
		baseShape.setPosition(new Vector2(0, -halfHeight));
		body.createFixture(baseShape, 0);
		baseShape.dispose();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(halfWidth, halfHeight);
		body.createFixture(shape, 1);
		shape.dispose();
		body.setBullet(true);
		body.setFixedRotation(true);
		body.setTransform(position.x, position.y, 0);
		entity.attach(new BodyPart(body));

		Limb root = new Limb()
		.addJoint(torso, 0, 0, 0.05f, 0.05f, 90)
		.addJoint(leftLegJoint)
		.addJoint(rightLegJoint);
		LimbsPart limbsPart = new LimbsPart(root, leftForearm, rightForearm, leftLeg, rightLeg, torso, head);
		Transform transform = new Box2dTransform(position.z, body);
		entity.attach(new TransformPart(transform), new CollisionPart());
		LimbAnimation walkAnimation = new WalkAnimation(leftLegJoint, rightLegJoint, new FloatRange(-110, -70));
		Map<String, LimbAnimation> animations = new HashMap<String, LimbAnimation>();
		animations.put("walk", walkAnimation);
		LimbAnimationsPart limbAnimationsPart = new LimbAnimationsPart(animations);
		entity.attach(limbAnimationsPart);
		Rotator rotator = new Rotator(rightBicepJoint, new FloatRange(-180, -45), 135);
		entity.attach(new MovementPart(5, 5, leftLeg, rightLeg));
		Alliance targetAlliance = alliance == Alliance.PLAYER ? Alliance.ENEMY : Alliance.PLAYER;
		entity.attach(new WeaponPart(targetAlliance.name(), new Centrum(gun.getTransform(), new Vector2(0.4f, 0.25f)), 0.3f, rotator),
				limbsPart, new VitalLimbsPart(head, torso));
		if (alliance == Alliance.ENEMY){
			entity.attach(new AiPart());
		}

		entityManager.add(entity);
		return entity;
	}

	public final void createBullet(final Centrum centrum, final String type) {
		Alliance alliance = Alliance.valueOf(type); // TODO: hacky...
		Vector2 size = new Vector2(0.08f, 0.08f);
		Vector2 relativeCenter = PolygonUtils.relativeCenter(centrum.getPosition(), size);
		Vector3 position3 = new Vector3(relativeCenter.x, relativeCenter.y, 0);
		Entity bullet = createBaseEntity(size, position3, "objects/bullet", new Enum<?>[] { CollisionType.METAL });
		bullet.attach(new AutoRotatePart(), new TimedDeathPart(3), new CollisionDamagePart(10, alliance), new ForcePart(1, alliance));
		Vector2 velocity = new Vector2(15, 0).setAngle(centrum.getRotation());
		bullet.get(TranslatePart.class).setVelocity(velocity);
		Entity trail = createBaseEntity(new Vector2(1.5f, 0.08f), new Vector3(), "objects/bullet_trail");
		trail.attach(new ScalePart(new FloatRange(0, 1), 0.2f));
		entityManager.add(trail);
		Limb trailLimb = new Limb(trail.get(TransformPart.class).getTransform());
		Transform transform = bullet.get(TransformPart.class).getTransform();
		Limb root = new Limb(transform).addJoint(trailLimb, 0.04f, 0.04f, 1.46f, 0.04f, 0);
		LimbsPart limbsPart = new LimbsPart(root, root);
		bullet.attach(limbsPart, new CollisionRemovePart(alliance));
		entityManager.add(bullet);
	}

	public final void createBloodParticle(final float size, final Vector3 position, final Vector2 velocity) {
		Entity entity = createBaseEntity(new Vector2(size, size), position, "objects/blood");
		entity.get(TranslatePart.class).setVelocity(velocity);
		entity.attach(new CollisionRemovePart(), new TimedDeathPart(3), new StickyPart());
		entityManager.add(entity);
	}

	private final void createLimbEntity(final Limb limb, final float startZ, final Limb[] zOrder, final Vector2 size, final String regionName, final float health, final Alliance alliance) {
		createLimbEntity(limb, startZ, zOrder, size, regionName, health, alliance, CollisionType.FLESH);
	}

	private final void createLimbEntity(final Limb limb, final float startZ, final Limb[] zOrder, final Vector2 size, final String regionName, final float health, final Alliance alliance, final CollisionType collisionType) {
		float z = startZ + ArrayUtils.indexOf(zOrder, limb) * MathUtils.FLOAT_ROUNDING_ERROR;
		Entity entity = createBaseEntity(size, new Vector3(0, 0, z), regionName, new Enum<?>[] { alliance, collisionType });
		entity.attach(new HealthPart(health));
		limb.setTransform(entity.get(TransformPart.class).getTransform());
		entityManager.add(entity);
	}

	private final Entity createBaseEntity(final Vector2 size, final Vector3 position, final String regionName) {
		return createBaseEntity(size, position, regionName, new Enum<?>[0]);
	}

	private final Entity createBaseEntity(final Vector2 size, final Vector3 position, final String regionName, final Enum<?>[] collisionGroups) {
		Entity entity = new Entity();
		Polygon polygon = convexHullCache.create(regionName, size);
		Transform transform = createTransform(polygon, position.z);
		transform.setPosition(new Vector2(position.x, position.y));
		entity.attach(new TransformPart(transform), new TranslatePart(), new CollisionPart(collisionGroups));
		PolygonRegion region = textureCache.getPolygonRegion(regionName);
		DrawablePart drawablePart = new DrawablePart(region);
		entity.attach(drawablePart);
		return entity;
	}

	private final Transform createTransform(final Polygon polygon, final float z) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		Body body = world.createBody(bodyDef);
		float[] vertices = polygon.getVertices();
		Array<Vector2> vertexVectors = new Array<Vector2>(VertexUtils.toVectors(vertices));
		for (Array<Vector2> partition : BayazitDecomposer.convexPartition(vertexVectors)) {
			PolygonShape shape = new PolygonShape();
			Vector2[] partitionVectors = partition.toArray(Vector2.class);
			shape.set(VertexUtils.toFloats(partitionVectors));
			body.createFixture(shape, 1).setSensor(true);
			shape.dispose();
		}
		return new Box2dTransform(z, body);
	}

}
