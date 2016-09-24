package dc.targetman.level;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.PathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
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

		// TODO: Test code
		DefaultIndexedGraph graph = createIndexedGraph(map);
		PathFinder<DefaultNode> pathFinder = new IndexedAStarPathFinder<DefaultNode>(graph, true);
		GraphPath<DefaultNode> path = new DefaultGraphPath<DefaultNode>();
		pathFinder.searchNodePath(graph.getNodes().get(0), graph.getNodes().get(10), new Heuristic<DefaultNode>() {
			@Override
			public float estimate(final DefaultNode node, final DefaultNode endNode) {
				float leftGap = node.getLeft() - endNode.getRight();
				float rightGap = node.getRight() - endNode.getLeft();
				float yOffset = endNode.getY() - node.getY();
				return Math.min(leftGap, rightGap) + yOffset;
			}
		}, path);
	}

	public static final DefaultIndexedGraph createIndexedGraph(final TiledMap map) {
		List<DefaultNode> nodes = createNodes(map);
		return new DefaultIndexedGraph(nodes);
	}

	private static List<DefaultNode> createNodes(final TiledMap map) {
		TiledMapTileLayer collisionLayer = getCollisionLayer(map);
		List<DefaultNode> nodes = new ArrayList<DefaultNode>();
		for (int y = 0; y < collisionLayer.getHeight() - 1; y++) {
			for (int x = 0; x < collisionLayer.getWidth(); x++) {
				int floorLength = getFloorLength(collisionLayer, x, y);
				if (floorLength > 0) {
					Vector2 position = new Vector2(x, y);
					nodes.add(new DefaultNode(position, floorLength));
				}
			}
		}
		return nodes;
	}

	private static int getFloorLength(final TiledMapTileLayer layer, final int x, final int y) {
		int floorLength = 0;
		for (int i = x; i < layer.getWidth() && layer.getCell(i, y) != null && layer.getCell(i, y + 1) != null; i++) {
			floorLength++;
		}
		return floorLength;
	}

	private static TiledMapTileLayer getCollisionLayer(final TiledMap map) {
		return (TiledMapTileLayer)map.getLayers().get(0);
	}

}
