package dc.targetman.level;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public final class MapUtils {

	private MapUtils() {
	}

	public static final TiledMapTileLayer getCollisionLayer(final TiledMap map) {
		return (TiledMapTileLayer)map.getLayers().get(0);
	}

}
