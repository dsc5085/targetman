package dc.targetman.level;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dc.targetman.mechanics.Alliance;
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
		TiledMapTileLayer collisionLayer = MapUtils.getCollisionLayer(map);
		for (int x = 0; x < collisionLayer.getWidth(); x++) {
			for (int y = 0; y < collisionLayer.getHeight(); y++) {
				Cell cell = collisionLayer.getCell(x, y);
				if (cell != null) {
					entityFactory.createWall(new Vector2(1, 1), new Vector3(x, y, 0));
				}
			}
		}
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
