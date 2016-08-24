package dc.targetman.level;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dc.targetman.epf.parts.ScalePart;
import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.level.models.CollisionGroup;
import dc.targetman.limb.WalkAnimation;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.AutoRotatePart;
import dclib.epf.parts.DrawablePart;
import dclib.epf.parts.LimbAnimationsPart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.PhysicsPart;
import dclib.epf.parts.TimedDeathPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.geometry.Centrum;
import dclib.geometry.PolygonFactory;
import dclib.geometry.PolygonUtils;
import dclib.graphics.ConvexHullCache;
import dclib.graphics.TextureCache;
import dclib.limb.Joint;
import dclib.limb.Limb;
import dclib.limb.LimbAnimation;
import dclib.limb.Rotator;
import dclib.physics.BodyType;
import dclib.util.FloatRange;

public final class EntityFactory {

	private final EntityManager entityManager;
	private final TextureCache textureCache;
	private final ConvexHullCache convexHullCache;

	public EntityFactory(final EntityManager entityManager, final TextureCache textureCache) {
		this.entityManager = entityManager;
		this.textureCache = textureCache;
		convexHullCache = new ConvexHullCache(textureCache);
	}

	public final void createWall(final Vector2 size, final Vector3 position) {
		Entity entity = createBaseEntity(size, position, "objects/white", BodyType.STATIC);
		entityManager.add(entity);
	}

	public final Entity createTargetman(final Vector3 position) {
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
		createLimbEntity(leftForearm, startZ, zOrder, new Vector2(0.4f, 0.1f), "objects/limb");
		createLimbEntity(leftBicep, startZ, zOrder, new Vector2(0.4f, 0.1f), "objects/limb");
		createLimbEntity(gun, startZ, zOrder, new Vector2(0.4f, 0.3f), "objects/gun");
		createLimbEntity(rightForearm, startZ, zOrder, new Vector2(0.4f, 0.1f), "objects/limb");
		createLimbEntity(rightBicep, startZ, zOrder, new Vector2(0.4f, 0.1f), "objects/limb");
		createLimbEntity(head, startZ, zOrder, new Vector2(0.5f, 0.5f), "objects/head");
		createLimbEntity(torso, startZ, zOrder, new Vector2(1, 0.1f), "objects/limb");
		createLimbEntity(leftLeg, startZ, zOrder, new Vector2(1, 0.1f), "objects/limb");
		createLimbEntity(rightLeg, startZ, zOrder, new Vector2(1, 0.1f), "objects/limb");
		Joint leftLegJoint = new Joint(leftLeg, new Vector2(), new Vector2(0, 0.05f), -110);
		Joint rightLegJoint = new Joint(rightLeg, new Vector2(), new Vector2(0, 0.05f), -70);
		Polygon polygon = PolygonFactory.createDefault();
		polygon.setPosition(position.x, position.y);
		TransformPart transformPart = new TransformPart(polygon, position.z);
		Limb root = new Limb(polygon)
		.addJoint(torso, 0, 0, 0.05f, 0.05f, 90)
		.addJoint(leftLegJoint)
		.addJoint(rightLegJoint);
		Entity entity = new Entity();
		entity.attach(transformPart);
		entity.attach(new TranslatePart());
		entity.attach(new PhysicsPart(BodyType.DYNAMIC, new CollisionGroup[0]));
		LimbsPart limbsPart = new LimbsPart(root, Arrays.asList(leftLeg.getPolygon(), rightLeg.getPolygon(), torso.getPolygon(), head.getPolygon()));
		entity.attach(limbsPart);
		LimbAnimation walkAnimation = new WalkAnimation(leftLegJoint, rightLegJoint, new FloatRange(-110, -70));
		Map<String, LimbAnimation> animations = new HashMap<String, LimbAnimation>();
		animations.put("walk", walkAnimation);
		LimbAnimationsPart limbAnimationsPart = new LimbAnimationsPart(animations);
		entity.attach(limbAnimationsPart);
		Rotator rotator = new Rotator(rightBicepJoint, new FloatRange(-180, -45), 135);
		entity.attach(new WeaponPart("", new Centrum(gun.getPolygon(), new Vector2(0.4f, 0.25f)), 0.3f, rotator));
		entityManager.add(entity);
		return entity;
	}

	public final void createBullet(final Centrum centrum) {
		Vector2 size = new Vector2(0.08f, 0.08f);
		Vector2 relativeCenter = PolygonUtils.relativeCenter(centrum.getPosition(), size);
		Vector3 position3 = new Vector3(relativeCenter.x, relativeCenter.y, 0);
		Entity bullet = createBaseEntity(size, position3, "objects/bullet", BodyType.DYNAMIC, new CollisionGroup[] { CollisionGroup.BULLET });
		bullet.get(PhysicsPart.class).setGravityScale(0.05f);
		bullet.attach(new AutoRotatePart());
		bullet.attach(new TimedDeathPart(3));
		Vector2 velocity = new Vector2(15, 0).setAngle(centrum.getRotation());
		bullet.get(TranslatePart.class).setVelocity(velocity);
		Entity entity = createBaseEntity(new Vector2(1.5f, 0.08f), new Vector3(), "objects/bullet_trail", BodyType.NONE);
		entity.attach(new ScalePart(new FloatRange(0, 1), 0.2f));
		entityManager.add(entity);
		Limb trail = new Limb(entity.get(TransformPart.class).getPolygon());
		Polygon polygon = bullet.get(TransformPart.class).getPolygon();
		Limb root = new Limb(polygon).addJoint(trail, 0.04f, 0.04f, 1.46f, 0.04f, 0);
		LimbsPart limbsPart = new LimbsPart(root, Arrays.asList(polygon));
		bullet.attach(limbsPart);
		entityManager.add(bullet);
	}

	private final void createLimbEntity(final Limb limb, final float startZ, final Limb[] zOrder, final Vector2 size, final String regionName) {
		float z = startZ + ArrayUtils.indexOf(zOrder, limb) * MathUtils.FLOAT_ROUNDING_ERROR;
		Entity entity = createBaseEntity(size, new Vector3(0, 0, z), regionName, BodyType.NONE);
		limb.setPolygon(entity.get(TransformPart.class).getPolygon());
		entityManager.add(entity);
	}

	private final Entity createBaseEntity(final Vector2 size, final Vector3 position, final String regionName,
			final BodyType bodyType) {
		return createBaseEntity(size, position, regionName, bodyType, new CollisionGroup[0]);
	}

	private final Entity createBaseEntity(final Vector2 size, final Vector3 position, final String regionName,
			final BodyType bodyType, final CollisionGroup[] collisionGroups) {
		Entity entity = new Entity();
		Polygon polygon = convexHullCache.create(regionName, size);
		polygon.setPosition(position.x,  position.y);
		entity.attach(new TransformPart(polygon, position.z));
		entity.attach(new TranslatePart());
		entity.attach(new PhysicsPart(bodyType, collisionGroups));
		PolygonRegion region = textureCache.getPolygonRegion(regionName);
		DrawablePart drawablePart = new DrawablePart(region);
		entity.attach(drawablePart);
		return entity;
	}

}
