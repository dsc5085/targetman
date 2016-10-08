package dc.targetman.ai;

import java.util.ArrayList;
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
import com.google.common.collect.Lists;

import dc.targetman.level.MapUtils;
import dclib.geometry.RectangleUtils;
import dclib.geometry.UnitConverter;
import dclib.util.Maths;

public final class GraphHelper {

	private final DefaultIndexedGraph graph;
	private final PathFinder<DefaultNode> pathFinder;

	public GraphHelper(final TiledMap map, final UnitConverter unitConverter) {
		graph = createGraph(map, unitConverter);
		pathFinder = new IndexedAStarPathFinder<DefaultNode>(graph, true);
	}

	public final DefaultNode getTouchingNode(final Rectangle bounds) {
		for (DefaultNode node : graph.getNodes()) {
			if (node.isTouching(bounds)) {
				return node;
			}
		}
		return null;
	}

	public final DefaultNode getTargetNode(final Rectangle bounds) {
		DefaultNode nearestNode = null;
		Vector2 center = bounds.getCenter(new Vector2());
		for (DefaultNode node : graph.getNodes()) {
			if (node.isTouching(bounds)) {
				return node;
			} else if (RectangleUtils.containsX(node.getBounds(), center.x)
					&& (nearestNode == null || Maths.between(node.top(), nearestNode.top(), center.y))) {
				nearestNode = node;
			}
		}
		return nearestNode;
	}

	// TODO: Pass in node instead of endBounds
	public final List<DefaultNode> createPath(final DefaultNode startNode, final Rectangle endBounds) {
		GraphPath<DefaultNode> path = new DefaultGraphPath<DefaultNode>();
		DefaultNode endNode = getTargetNode(endBounds);
		if (startNode != null && endNode != null) {
			pathFinder.searchNodePath(startNode, endNode, getHeuristic(), path);
		}
		return Lists.newArrayList(path.iterator());
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

	private Heuristic<DefaultNode> getHeuristic() {
		return new Heuristic<DefaultNode>() {
			@Override
			public float estimate(final DefaultNode node, final DefaultNode endNode) {
				float xOffset = Maths.distance(node.left(), endNode.left());
				float yOffset = Maths.distance(endNode.top(), node.top());
				return xOffset + yOffset;
			}
		};
	}

}
