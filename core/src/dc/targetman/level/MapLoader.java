package dc.targetman.level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.ImmutableSet;

import dc.targetman.mechanics.Alliance;
import dclib.geometry.Point;
import dclib.geometry.PolygonUtils;
import dclib.graphics.ScreenHelper;

public final class MapLoader {

	private final TiledMap map;
	private final ScreenHelper screenHelper;
	private final EntityFactory entityFactory;

	public MapLoader(final TiledMap map, final ScreenHelper screenHelper, final EntityFactory entityFactory) {
		this.map = map;
		this.screenHelper = screenHelper;
		this.entityFactory = entityFactory;
	}

	public final void createObjects() {
		createStaticObjects();
		createActors();
	}

	public final void createStaticObjects() {
		Map<Point, List<Vector2>> pointToWallVertices = new HashMap<Point, List<Vector2>>();
		TiledMapTileLayer collisionLayer = MapUtils.getCollisionLayer(map);
		for (int x = 0; x < collisionLayer.getWidth(); x++) {
			for (int y = 0; y < collisionLayer.getHeight(); y++) {
				if (collisionLayer.getCell(x, y) != null) {
					addTileVertices(pointToWallVertices, x, y);
				}
			}
		}
		Set<List<Vector2>> wallsVertices = ImmutableSet.copyOf(pointToWallVertices.values());
		for (List<Vector2> wallVertices : wallsVertices) {
			entityFactory.createWall(wallVertices);
		}
	}

	private void addTileVertices(final Map<Point, List<Vector2>> pointToWallVertices, final int x, final int y) {
		float[] vertices = PolygonUtils.createRectangleVertices(new Rectangle(x, y, 1, 1));
		List<Vector2> newVertices = PolygonUtils.toVectors(vertices);
		List<Vector2> wallVertices = newVertices;
		Point point = new Point(x, y);
		// due to the order at which tiles are iterated, current neighbors can only exist at left or bottom
		Point left = new Point(x - 1, y);
		Point bottom = new Point(x, y - 1);
		if (pointToWallVertices.containsKey(left)) {
			wallVertices = pointToWallVertices.get(left);
			PolygonOperations.union(wallVertices, newVertices);
		} else if (pointToWallVertices.containsKey(bottom)) {
			wallVertices = pointToWallVertices.get(bottom);
			PolygonOperations.union(wallVertices, newVertices);
		}
		pointToWallVertices.put(point, wallVertices);
	}

	private void createActors() {
		MapLayer layer = map.getLayers().get(1);
		for (TiledMapTileMapObject object : layer.getObjects().getByType(TiledMapTileMapObject.class)) {
			Vector3 position = getWorldPosition(object, 0);
			String allianceString = object.getTile().getProperties().get(Alliance.class.getSimpleName(), String.class);
			entityFactory.createStickman(position, Alliance.valueOf(allianceString));
		}
	}

	private Vector3 getWorldPosition(final TiledMapTileMapObject object, final float z) {
		Vector2 worldPosition = screenHelper.toWorldUnits(object.getX(), object.getY());
		return new Vector3(worldPosition.x, worldPosition.y, 0);
	}

}
