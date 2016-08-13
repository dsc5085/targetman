package dc.targetman.level;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dclib.epf.Entity;
import dclib.epf.parts.DrawablePart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.graphics.ConvexHullCache;
import dclib.graphics.TextureCache;

public final class EntityFactory {

	private final TextureCache textureCache;
	private final ConvexHullCache convexHullCache;

	public EntityFactory(final TextureCache textureCache) {
		this.textureCache = textureCache;
		convexHullCache = new ConvexHullCache(textureCache);
	}

	public final Entity createTargetman(final Vector2 size, final Vector3 position) {
		PolygonRegion region = textureCache.getPolygonRegion("objects/bluepixel");
		Polygon polygon = convexHullCache.create("objects/bluepixel", size);
		polygon.setPosition(position.x, position.y);
		Entity entity = createBaseEntity(polygon, position.z, region);
		return entity;
	}

	private final Entity createBaseEntity(final Polygon polygon, final float z, final PolygonRegion region) {
		Entity entity = new Entity();
		entity.attach(new TransformPart(polygon, z));
		entity.attach(new TranslatePart());
		DrawablePart drawablePart = new DrawablePart(region);
		entity.attach(drawablePart);
		return entity;
	}

}
