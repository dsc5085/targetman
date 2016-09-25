package dc.targetman.level;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public final class MapUtils {

	private MapUtils() {
	}

	public static final void spawn(final TiledMap map, final EntityFactory entityFactory) {
		TiledMapTileLayer collisionLayer = getCollisionLayer(map);
		for (int x = 0; x < collisionLayer.getWidth(); x++) {
			for (int y = 0; y < collisionLayer.getHeight(); y++) {
				Cell cell = collisionLayer.getCell(x, y);
				if (cell != null) {
					entityFactory.createWall(new Vector2(1, 1), new Vector3(x, y, 0));
				}
			}
		}
	}

	public static TiledMapTileLayer getCollisionLayer(final TiledMap map) {
		return (TiledMapTileLayer)map.getLayers().get(0);
	}

}
