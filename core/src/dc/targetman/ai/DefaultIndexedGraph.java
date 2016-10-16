package dc.targetman.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import dclib.geometry.RectangleUtils;
import dclib.util.CollectionUtils;
import dclib.util.Maths;

public final class DefaultIndexedGraph implements IndexedGraph<DefaultNode> {

	private final List<DefaultNode> nodes = new ArrayList<DefaultNode>();
	private final Map<DefaultNode, Array<Connection<DefaultNode>>> nodeConnections
	= new HashMap<DefaultNode, Array<Connection<DefaultNode>>>();

	public DefaultIndexedGraph(final List<Rectangle> boundsList) {
		List<Segment> segments = createSegments(boundsList);
		connect(segments);
		for (Segment segment : segments) {
			nodes.addAll(segment.nodes);
		}
	}

	@Override
	public final Array<Connection<DefaultNode>> getConnections(final DefaultNode fromNode) {
		return CollectionUtils.get(nodeConnections, fromNode, new Array<Connection<DefaultNode>>());
	}

	@Override
	public final int getNodeCount() {
		return nodes.size();
	}

	@Override
	public final int getIndex(final DefaultNode node) {
		return nodes.indexOf(node);
	}

	public final List<DefaultNode> getNodes() {
		return new ArrayList<DefaultNode>(nodes);
	}

	private List<Segment> createSegments(final List<Rectangle> boundsList) {
		List<Segment> segments = new ArrayList<Segment>();
		for (Rectangle bounds : boundsList) {
			Segment segment = new Segment(bounds);
			addConnection(segment.leftNode, segment.rightNode);
			addConnection(segment.rightNode, segment.leftNode);
			segments.add(segment);
		}
		return segments;
	}

	private void addConnection(final DefaultNode startNode, final DefaultNode endNode) {
		Connection<DefaultNode> connection = new DefaultConnection<DefaultNode>(startNode, endNode);
		getConnections(startNode).add(connection);
	}

	private void connect(final List<Segment> segments) {
		for (int i = 0; i < segments.size() - 1; i++) {
			Segment segment1 = segments.get(i);
			for (int j = i + 1; j < segments.size(); j++) {
				Segment segment2 = segments.get(j);
				connect(segment1, segment2);
			}
		}
	}

	private void connect(final Segment segment1, final Segment segment2) {
		connect(segment1.leftNode, segment2.rightNode);
		connect(segment1.rightNode, segment2.leftNode);
		connect(segment2.leftNode, segment1.rightNode);
		connect(segment2.rightNode, segment1.leftNode);
		connectMiddle(segment1, segment2);
		connectMiddle(segment2, segment1);
	}

	private void connect(final DefaultNode startNode, final DefaultNode endNode) {
		if (canJumpTo(startNode, endNode)) {
			addConnection(startNode, endNode);
		}
	}

	private boolean canJumpTo(final DefaultNode startNode, final DefaultNode endNode) {
		// TODO: Replace these constants with calculations
		final float jumpWidth = 8;
		final float jumpHeight = 5;
		float gapWidth = Maths.distance(startNode.x(), endNode.x());
		boolean canJumpToHorizontally = gapWidth < jumpWidth;
		float yOffset = startNode.y() - endNode.y();
		boolean canJumpToVertically = yOffset < jumpHeight;
		return canJumpToHorizontally && canJumpToVertically;
	}

	private void connectMiddle(final Segment topSegment, final Segment bottomSegment) {
		if (topSegment.y > bottomSegment.y) {
			connectMiddle(topSegment.leftNode, bottomSegment);
			connectMiddle(topSegment.rightNode, bottomSegment);
		}
	}

	private void connectMiddle(final DefaultNode topNode, final Segment bottomSegment) {
		float bottomLeft = bottomSegment.leftNode.x();
		float bottomRight = bottomSegment.rightNode.x();
		if (Maths.between(topNode.x(), bottomLeft, bottomRight)) {
			DefaultNode bottomNode = new DefaultNode(topNode.x(), bottomSegment.y);
			bottomSegment.nodes.add(bottomNode);
			connect(topNode, bottomNode);
			connect(bottomNode, topNode);
		}
	}

	private class Segment {

		public final DefaultNode leftNode;
		public final DefaultNode rightNode;
		public final List<DefaultNode> nodes = new ArrayList<DefaultNode>();
		public final float y;

		public Segment(final Rectangle bounds) {
			y = RectangleUtils.top(bounds);
			leftNode = new DefaultNode(bounds.x, y);
			nodes.add(leftNode);
			rightNode = new DefaultNode(RectangleUtils.right(bounds), y);
			nodes.add(rightNode);
		}

	}

}
