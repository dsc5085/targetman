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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import dc.targetman.level.MapUtils;
import dclib.geometry.UnitConverter;
import dclib.physics.Box2dUtils;
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

	public final Segment getBelowSegment(final Rectangle bounds) {
		Segment belowSegment = null;
		for (Segment segment : graph.getSegments()) {
			boolean isNearerY = belowSegment == null || Maths.between(segment.y, belowSegment.y, bounds.y);
			if (isNearerY && segment.overlapsX(bounds)) {
				belowSegment = segment;
			}
		}
		return belowSegment;
	}

	public final Segment getTouchingSegment(final Rectangle bounds) {
		Rectangle collisionBounds = Box2dUtils.collisionBounds(bounds);
		for (Segment segment : graph.getSegments()) {
			if (collisionBounds.overlaps(segment.bounds)) {
				return segment;
			}
		}
		return null;
	}

	public final Segment getSegment(final DefaultNode node) {
		for (Segment segment : graph.getSegments()) {
			if (segment.nodes.contains(node)) {
				return segment;
			}
		}
		return null;
	}

	public final List<DefaultNode> createPath(final Segment startSegment, final DefaultNode endNode) {
		GraphPath<DefaultNode> lowestCostPath = new DefaultGraphPath<DefaultNode>();
		for (DefaultNode startNode : startSegment.nodes) {
			GraphPath<DefaultNode> path = new DefaultGraphPath<DefaultNode>();
			pathFinder.searchNodePath(startNode, endNode, getHeuristic(), path);
			if (Iterables.isEmpty(lowestCostPath) || getCost(path) < getCost(lowestCostPath)) {
				lowestCostPath = path;
			}
		}
		return Lists.newArrayList(lowestCostPath);
	}

	private DefaultIndexedGraph createGraph(final TiledMap map, final UnitConverter unitConverter) {
		TiledMapTileLayer collisionLayer = MapUtils.getCollisionLayer(map);
		List<Rectangle> boundsList = new ArrayList<Rectangle>();
		Vector2 size = unitConverter.toWorldUnits(collisionLayer.getTileWidth(), collisionLayer.getTileHeight());
		for (int y = 0; y < collisionLayer.getHeight() - 1; y++) {
			for (int x = 0; x < collisionLayer.getWidth(); x++) {
				int floorLength = getFloorLength(collisionLayer, x, y);
				if (floorLength > 0) {
					Rectangle bounds = new Rectangle(x, y, floorLength * size.x, size.y);
					boundsList.add(bounds);
					x += floorLength;
				}
			}
		}
		return new DefaultIndexedGraph(boundsList);
	}

	private int getFloorLength(final TiledMapTileLayer layer, final int x, final int y) {
		int floorLength = 0;
		for (int i = x; i < layer.getWidth() && layer.getCell(i, y) != null && layer.getCell(i, y + 1) == null; i++) {
			floorLength++;
		}
		return floorLength;
	}

	private float getCost(final GraphPath<DefaultNode> path) {
		float cost = 0;
		Heuristic<DefaultNode> heuristic = getHeuristic();
		for (int i = 0; i < path.getCount() - 1; i++) {
			DefaultNode startNode = path.get(i);
			DefaultNode endNode = path.get(i + 1);
			cost += heuristic.estimate(startNode, endNode);
		}
		return cost;
	}

	private Heuristic<DefaultNode> getHeuristic() {
		return new Heuristic<DefaultNode>() {
			@Override
			public float estimate(final DefaultNode node, final DefaultNode endNode) {
				float xOffset = Maths.distance(node.x(), endNode.x());
				float yOffset = Maths.distance(endNode.y(), node.y());
				return xOffset + yOffset;
			}
		};
	}

}
