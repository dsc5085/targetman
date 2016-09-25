package dc.targetman.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.PathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dc.targetman.level.DefaultIndexedGraph;
import dc.targetman.level.DefaultNode;
import dc.targetman.level.MapUtils;
import dclib.geometry.UnitConverter;
import dclib.util.Maths;

public final class PathCreator {

	private final DefaultIndexedGraph graph;
	private final PathFinder<DefaultNode> pathFinder;

	public PathCreator(final TiledMap map, final UnitConverter unitConverter) {
		graph = createGraph(map, unitConverter);
		pathFinder = new IndexedAStarPathFinder<DefaultNode>(graph, true);
	}

	public GraphPath<DefaultNode> createPath(final Vector2 start, final Vector2 end) {
		GraphPath<DefaultNode> path = new DefaultGraphPath<DefaultNode>();
		DefaultNode startNode = getNode(start);
		DefaultNode endNode = getNode(end);
		pathFinder.searchNodePath(startNode, endNode, new Heuristic<DefaultNode>() {
			@Override
			public float estimate(final DefaultNode node, final DefaultNode endNode) {
				float xOffset = Maths.distance(node.x(), endNode.x());
				float yOffset = Maths.distance(endNode.top(), node.top());
				return xOffset + yOffset;
			}
		}, path);
		return path;
	}

	private DefaultIndexedGraph createGraph(final TiledMap map, final UnitConverter unitConverter) {
		TiledMapTileLayer collisionLayer = MapUtils.getCollisionLayer(map);
		List<DefaultNode> nodes = new ArrayList<DefaultNode>();
		Vector2 size = unitConverter.toWorldUnits(collisionLayer.getTileWidth(), collisionLayer.getTileHeight());
		for (int y = 0; y < collisionLayer.getHeight() - 1; y++) {
			for (int x = 0; x < collisionLayer.getWidth(); x++) {
				int floorLength = getFloorLength(collisionLayer, x, y);
				if (floorLength > 0) {
					Rectangle bounds = new Rectangle(x, y, floorLength * size.x, size.y);
					nodes.add(new DefaultNode(bounds));
					x += floorLength;
				}
			}
		}
		return new DefaultIndexedGraph(nodes);
	}

	private int getFloorLength(final TiledMapTileLayer layer, final int x, final int y) {
		int floorLength = 0;
		for (int i = x; i < layer.getWidth() && layer.getCell(i, y) != null && layer.getCell(i, y + 1) == null; i++) {
			floorLength++;
		}
		return floorLength;
	}

	private DefaultNode getNode(final Vector2 position) {
		NodeDistanceComparator comparator = new NodeDistanceComparator(position);
		return Collections.min(graph.getNodes(), comparator);
	}

	private class NodeDistanceComparator implements Comparator<DefaultNode> {

		private final Vector2 position;

		public NodeDistanceComparator(final Vector2 position) {
			this.position = position;
		}

		@Override
		public int compare(final DefaultNode node1, final DefaultNode node2) {
			return Float.compare(getCost(node1), getCost(node2));
		}

		private float getCost(final DefaultNode node) {
			// TODO: inaccurate
			return Maths.distance(node.x(), position.x) + Maths.distance(node.right(), position.x)
			+ Maths.distance(node.top(), position.y);
		}

	}

}
