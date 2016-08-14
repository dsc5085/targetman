package dc.targetman.level;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dclib.epf.Entity;
import dclib.epf.parts.DrawablePart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.PhysicsPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.geometry.PolygonFactory;
import dclib.geometry.RectangleUtils;
import dclib.graphics.ConvexHullCache;
import dclib.graphics.TextureCache;
import dclib.physics.BodyType;
import dclib.physics.Limb;

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

	public final List<Entity> createTargetman(final Vector2 size, final Vector3 position) {
		List<Entity> entities = new ArrayList<Entity>();
		Limb leftArmLimb = createLimb(entities, new Vector2(0.5f, 0.2f));
		Limb rightArmLimb = createLimb(entities, new Vector2(0.5f, 0.2f));
		Limb headLimb = createLimb(entities, new Vector2(0.5f, 0.5f));
		Limb torsoLimb = createLimb(entities, new Vector2(0.5f, 0.2f))
				.addJoint(leftArmLimb, 0.4f, 0.1f, 0, 0.1f, -135)
				.addJoint(rightArmLimb, 0.4f, 0.1f, 0, 0.1f, -225)
				.addJoint(headLimb, 0.5f, 0.1f, 0, 0.25f, 0);
		Limb leftLeg = createLimb(entities, new Vector2(0.5f, 0.2f));
		Limb rightLeg = createLimb(entities, new Vector2(0.5f, 0.2f));
		Limb root = new Limb()
		.addJoint(torsoLimb, 0, 0, 0, 0.1f, 90)
		.addJoint(leftLeg, 0, 0, 0, 0.1f, -135)
		.addJoint(rightLeg, 0, 0, 0, 0.1f, -45);
		Entity entity = new Entity();
		Rectangle bounds = getBounds(headLimb, torsoLimb, leftLeg, rightLeg);
		Polygon polygon = new Polygon(PolygonFactory.createRectangleVertices(bounds));
		root.setPolygon(polygon);
		TransformPart transformPart = new TransformPart(polygon, position.z);
		transformPart.setPosition(new Vector2(position.x, position.y));
		entity.attach(transformPart);
		entity.attach(new TranslatePart());
		entity.attach(new PhysicsPart(BodyType.DYNAMIC));
		LimbsPart limbsPart = new LimbsPart(root);
		entity.attach(limbsPart);
		entities.add(entity);
		return entities;
	}

	public final Limb createLimb(final List<Entity> entities, final Vector2 size) {
		Entity entity = createBaseEntity(size, new Vector3(), "objects/bluepixel", BodyType.NONE);
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

	private final Rectangle getBounds(final Limb... limbs) {
		Vector2 min = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
		Vector2 max = new Vector2(-Float.MAX_VALUE, -Float.MAX_VALUE);
		for (Limb limb : limbs) {
			Rectangle limbBounds = limb.getPolygon().getBoundingRectangle();
			min.x = Math.min(min.x, limbBounds.x);
			min.y = Math.min(min.y, limbBounds.y);
			max.x = Math.max(max.x, RectangleUtils.right(limbBounds));
			max.y = Math.max(max.y, RectangleUtils.top(limbBounds));
		}
		return new Rectangle(min.x, min.y, max.x - min.x, max.y - min.y);
	}

}
