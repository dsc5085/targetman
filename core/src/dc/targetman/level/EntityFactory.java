package dc.targetman.level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.limb.WalkAnimation;
import dclib.epf.Entity;
import dclib.epf.parts.AutoRotatePart;
import dclib.epf.parts.DrawablePart;
import dclib.epf.parts.LimbAnimationsPart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.PhysicsPart;
import dclib.epf.parts.TimedDeathPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.geometry.Centrum;
import dclib.geometry.PolygonUtils;
import dclib.graphics.ConvexHullCache;
import dclib.graphics.TextureCache;
import dclib.limb.Joint;
import dclib.limb.Limb;
import dclib.limb.LimbAnimation;
import dclib.physics.BodyType;
import dclib.util.FloatRange;

public final class EntityFactory {

	private final TextureCache textureCache;
	private final ConvexHullCache convexHullCache;

	public EntityFactory(final TextureCache textureCache) {
		this.textureCache = textureCache;
		convexHullCache = new ConvexHullCache(textureCache);
	}

	public final Entity createWall(final Vector2 size, final Vector3 position) {
		return createBaseEntity(size, position, "objects/bluepixel", BodyType.STATIC);
	}

	public final List<Entity> createTargetman(final Vector3 position) {
		List<Entity> entities = new ArrayList<Entity>();
		Limb leftForemanLimb = createLimb(entities, new Vector2(0.4f, 0.1f), "objects/limb");
		Limb leftBicepLimb = createLimb(entities, new Vector2(0.4f, 0.1f), "objects/limb")
				.addJoint(leftForemanLimb, 0.4f, 0.05f, 0, 0.05f, 90);
		Limb gun = createLimb(entities, new Vector2(0.4f, 0.3f), "objects/gun");
		Limb rightForemanLimb = createLimb(entities, new Vector2(0.4f, 0.1f), "objects/limb")
				.addJoint(gun, 0.4f, 0.05f, 0.1f, 0.05f, 0);
		Limb rightBicepLimb = createLimb(entities, new Vector2(0.4f, 0.1f), "objects/limb")
				.addJoint(rightForemanLimb, 0.4f, 0.05f, 0, 0.05f, 45);
		Limb headLimb = createLimb(entities, new Vector2(0.5f, 0.5f), "objects/head");
		Joint rightBicepJoint = new Joint(rightBicepLimb, new Vector2(0.8f, 0.05f), new Vector2(0, 0.05f), -135);
		Limb torsoLimb = createLimb(entities, new Vector2(1, 0.1f), "objects/limb")
				.addJoint(leftBicepLimb, 0.8f, 0.05f, 0, 0.05f, -225)
				.addJoint(rightBicepJoint)
				.addJoint(headLimb, 1, 0.05f, 0, 0.25f, 0);
		Limb leftLeg = createLimb(entities, new Vector2(1, 0.1f), "objects/limb");
		Limb rightLeg = createLimb(entities, new Vector2(1, 0.1f), "objects/limb");
		Joint leftLegJoint = new Joint(leftLeg, new Vector2(), new Vector2(0, 0.05f), -110);
		Joint rightLegJoint = new Joint(rightLeg, new Vector2(), new Vector2(0, 0.05f), -70);
		Limb root = new Limb(new Polygon())
		.addJoint(torsoLimb, 0, 0, 0, 0.05f, 90)
		.addJoint(leftLegJoint)
		.addJoint(rightLegJoint);
		Entity entity = new Entity();
		TransformPart transformPart = new TransformPart(new Polygon(new float[] { 0, 0, 1, 0, 0, 1 }), position.z);
		transformPart.setPosition(new Vector2(position.x, position.y));
		entity.attach(transformPart);
		entity.attach(new TranslatePart());
		entity.attach(new PhysicsPart(BodyType.DYNAMIC));
		LimbsPart limbsPart = new LimbsPart(root, Arrays.asList(leftLeg, rightLeg, torsoLimb, headLimb));
		entity.attach(limbsPart);
		entities.add(entity);
		LimbAnimation walkAnimation = new WalkAnimation(leftLegJoint, rightLegJoint, new FloatRange(-110, -70));
		Map<String, LimbAnimation> animations = new HashMap<String, LimbAnimation>();
		animations.put("walk", walkAnimation);
		LimbAnimationsPart limbAnimationsPart = new LimbAnimationsPart(animations);
		entity.attach(limbAnimationsPart);
		entity.attach(new WeaponPart("", new Centrum(gun.getPolygon(), new Vector2(0.4f, 0.3f)), 0.1f, rightBicepJoint, new FloatRange(-180, -45)));
		return entities;
	}

	public final Entity createBullet(final Vector2 position, final float rotation) {
		Vector2 size = new Vector2(0.05f, 0.05f);
		Vector2 relativeCenter = PolygonUtils.relativeCenter(position, size);
		Vector3 position3 = new Vector3(relativeCenter.x, relativeCenter.y, 0);
		Entity entity = createBaseEntity(size, position3, "objects/bullet", BodyType.NONE);
		entity.attach(new AutoRotatePart());
		entity.attach(new TimedDeathPart(3));
		Vector2 velocity = new Vector2(10, 0).setAngle(rotation);
		entity.get(TranslatePart.class).setVelocity(velocity);
		return entity;
	}

	private final Limb createLimb(final List<Entity> entities, final Vector2 size, final String regionName) {
		Entity entity = createBaseEntity(size, new Vector3(), regionName, BodyType.NONE);
		entities.add(entity);
		return new Limb(entity.get(TransformPart.class).getPolygon());
	}

	private final Entity createBaseEntity(final Vector2 size, final Vector3 position, final String regionName,
			final BodyType bodyType) {
		Entity entity = new Entity();
		Polygon polygon = convexHullCache.create(regionName, size);
		polygon.setPosition(position.x,  position.y);
		entity.attach(new TransformPart(polygon, position.z));
		entity.attach(new TranslatePart());
		entity.attach(new PhysicsPart(bodyType));
		PolygonRegion region = textureCache.getPolygonRegion(regionName);
		DrawablePart drawablePart = new DrawablePart(region);
		entity.attach(drawablePart);
		return entity;
	}

}
