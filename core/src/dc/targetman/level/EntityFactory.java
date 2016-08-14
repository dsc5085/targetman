package dc.targetman.level;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dclib.epf.Entity;
import dclib.epf.parts.DrawablePart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.PhysicsPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.graphics.ConvexHullCache;
import dclib.graphics.TextureCache;
import dclib.physics.BodyType;
import dclib.physics.Joint;
import dclib.physics.Limb;

public final class EntityFactory {

	private final TextureCache textureCache;
	private final ConvexHullCache convexHullCache;

	public EntityFactory(final TextureCache textureCache) {
		this.textureCache = textureCache;
		convexHullCache = new ConvexHullCache(textureCache);
	}

	public final Entity createWall(final Vector2 size, final Vector3 position) {
		return createBaseEntity(size, position, 0, "objects/bluepixel", BodyType.STATIC);
	}

	public final List<Entity> createTargetman(final Vector2 size, final Vector3 position) {
		List<Entity> entities = new ArrayList<Entity>();
		Entity entity = createBaseEntity(size, position, 0, "objects/transparent", BodyType.DYNAMIC);
		entities.add(entity);
		List<Joint> joints = new ArrayList<Joint>();
		Entity leftLeg = createBaseEntity(new Vector2(0.5f, 0.2f), new Vector3(0, 0, 0), 225, "objects/bluepixel",
				BodyType.NONE);
		addLimb(entities, joints, leftLeg, new Vector2(0, 0.1f));
		Entity rightLeg = createBaseEntity(new Vector2(0.5f, 0.2f), new Vector3(0, 0, 0), 315, "objects/bluepixel",
				BodyType.NONE);
		addLimb(entities, joints, rightLeg, new Vector2(0, 0.1f));
		Limb root = new Limb(entity.get(TransformPart.class).getPolygon(), new Vector2(), joints);
		entity.attach(new LimbsPart(root));
		return entities;
	}

	private void addLimb(final List<Entity> entities, final List<Joint> joints, final Entity limbEntity,
			final Vector2 parentJointLocal) {
		entities.add(limbEntity);
		Limb limb = new Limb(limbEntity.get(TransformPart.class).getPolygon(), parentJointLocal);
		joints.add(new Joint(limb, new Vector2()));
	}

	private final Entity createBaseEntity(final Vector2 size, final Vector3 position, final float rotation,
			final String regionName, final BodyType bodyType) {
		Entity entity = new Entity();
		Polygon polygon = convexHullCache.create(regionName, size);
		polygon.setPosition(position.x,  position.y);
		polygon.setRotation(rotation);
		entity.attach(new TransformPart(polygon, position.z));
		entity.attach(new TranslatePart());
		entity.attach(new PhysicsPart(bodyType));
		PolygonRegion region = textureCache.getPolygonRegion(regionName);
		DrawablePart drawablePart = new DrawablePart(region);
		entity.attach(drawablePart);
		return entity;
	}

}
