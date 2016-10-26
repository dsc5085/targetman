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
import com.google.common.collect.Iterables;
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

	public final DefaultNode getNearestNode(final float x, final Segment segment) {
		return Collections.min(segment.nodes, new Comparator<DefaultNode>() {
			@Override
			public int compare(final DefaultNode n1, final DefaultNode n2) {
				return Float.compare(getCost(x, n1), getCost(x, n2));
			}
		});
	}

	public final boolean isBelow(final DefaultNode node, final Rectangle bounds, final Segment belowSegment) {
		return belowSegment.nodes.contains(node) && RectangleUtils.containsX(bounds, node.x());
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

	public final Segment getSegment(final DefaultNode node) {
		for (Segment segment : graph.getSegments()) {
			if (segment.nodes.contains(node)) {
				return segment;
			}
		}
		return null;
	}

	public final List<DefaultNode> createPath(final float x, final Segment startSegment, final DefaultNode endNode) {
		GraphPath<DefaultNode> lowestCostPath = new DefaultGraphPath<DefaultNode>();
		// TODO: Use Collections.min here and in other places as well
		for (DefaultNode startNode : startSegment.nodes) {
			GraphPath<DefaultNode> path = new DefaultGraphPath<DefaultNode>();
			pathFinder.searchNodePath(startNode, endNode, getHeuristic(), path);
			if (Iterables.isEmpty(lowestCostPath) || getCost(x, path) < getCost(x, lowestCostPath)) {
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

	private float getCost(final float x, final GraphPath<DefaultNode> path) {
		float cost = path.getCount() > 0 ? getCost(x, path.get(0)) : 0;
		Heuristic<DefaultNode> heuristic = getHeuristic();
		for (int i = 0; i < path.getCount() - 1; i++) {
			DefaultNode startNode = path.get(i);
			DefaultNode endNode = path.get(i + 1);
			cost += heuristic.estimate(startNode, endNode);
		}
		return cost;
	}

	private float getCost(final float x, final DefaultNode node) {
		return Maths.distance(x, node.x());
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
