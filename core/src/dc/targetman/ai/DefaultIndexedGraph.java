package dc.targetman.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import dc.targetman.util.ArrayUtils;
import dclib.util.Maths;

public final class DefaultIndexedGraph implements IndexedGraph<DefaultNode> {

	private final List<Segment> segments;
	// TODO: Hold nodes variable or calculate on the fly from segments?
	private final List<DefaultNode> nodes = new ArrayList<DefaultNode>();

	public DefaultIndexedGraph(final List<Rectangle> boundsList) {
		segments = createSegments(boundsList);
		connect(segments);
		for (Segment segment : segments) {
			nodes.addAll(segment.nodes);
		}
	}

	@Override
	public final Array<Connection<DefaultNode>> getConnections(final DefaultNode fromNode) {
		return ArrayUtils.toArray(fromNode.getConnections());
	}

	@Override
	public final int getNodeCount() {
		return nodes.size();
	}

	@Override
	public final int getIndex(final DefaultNode node) {
		return nodes.indexOf(node);
	}

	public final List<Segment> getSegments() {
		return new ArrayList<Segment>(segments);
	}

	public final List<DefaultNode> getNodes() {
		return new ArrayList<DefaultNode>(nodes);
	}

	private List<Segment> createSegments(final List<Rectangle> boundsList) {
		List<Segment> segments = new ArrayList<Segment>();
		for (Rectangle bounds : boundsList) {
			Segment segment = new Segment(bounds);
			segments.add(segment);
		}
		return segments;
	}

	private void connect(final List<Segment> segments) {
		for (int i = 0; i < segments.size() - 1; i++) {
			Segment segment1 = segments.get(i);
			for (int j = i + 1; j < segments.size(); j++) {
				Segment segment2 = segments.get(j);
				connect(segment1, segment2);
			}
		}
		for (Segment segment : segments) {
			connectWithin(segment);
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

	private void connectMiddle(final Segment topSegment, final Segment bottomSegment) {
		if (topSegment.y > bottomSegment.y) {
			connectMiddle(topSegment.leftNode, bottomSegment);
			connectMiddle(topSegment.rightNode, bottomSegment);
		}
	}

	private void connectMiddle(final DefaultNode topNode, final Segment bottomSegment) {
		if (bottomSegment.containsX(topNode.x())) {
			DefaultNode bottomNode = new DefaultNode(topNode.x(), bottomSegment.y);
			bottomSegment.nodes.add(bottomNode);
			connect(topNode, bottomNode);
			connect(bottomNode, topNode);
		}
	}

	private void connect(final DefaultNode startNode, final DefaultNode endNode) {
		if (canJumpTo(startNode, endNode)) {
			startNode.addConnection(endNode);
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

	private void connectWithin(final Segment segment) {
		List<DefaultNode> nodes = new ArrayList<DefaultNode>(segment.nodes);
		Collections.sort(nodes, new Comparator<DefaultNode>() {
			@Override
			public int compare(final DefaultNode n1, final DefaultNode n2) {
				return Float.compare(n1.x(), n2.x());
			}
		});
		// TODO: left and right node don't get connected
		for (int i = 1; i < nodes.size() - 1; i++) {
			DefaultNode startNode = nodes.get(i);
			startNode.addConnection(nodes.get(i - 1));
			startNode.addConnection(nodes.get(i + 1));
		}
	}

}
