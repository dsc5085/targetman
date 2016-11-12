package dc.targetman.ai;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import dc.targetman.util.ArrayUtils;
import dclib.util.Maths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class DefaultIndexedGraph implements IndexedGraph<DefaultNode> {

	private final Vector2 actorSize;
	private final List<Segment> segments;
	// TODO: Hold nodes variable or calculate on the fly from segments?
	private final List<DefaultNode> nodes = new ArrayList<DefaultNode>();

	public DefaultIndexedGraph(final List<Rectangle> boundsList, final Vector2 actorSize) {
		this.actorSize = actorSize;
		segments = createSegments(boundsList);
		connect(segments);
		for (Segment segment : segments) {
            nodes.addAll(segment.getNodes());
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
        connect(segment1.getLeftNode(), segment2.getRightNode());
        connect(segment1.getRightNode(), segment2.getLeftNode());
        connect(segment2.getLeftNode(), segment1.getRightNode());
        connect(segment2.getRightNode(), segment1.getLeftNode());
        connectMiddle(segment1, segment2);
		connectMiddle(segment2, segment1);
	}

	private void connectMiddle(final Segment topSegment, final Segment bottomSegment) {
        if (topSegment.getY() > bottomSegment.getY()) {
            connectMiddle(topSegment.getLeftNode(), bottomSegment, -actorSize.x);
            connectMiddle(topSegment.getRightNode(), bottomSegment, actorSize.x);
        }
	}

	private void connectMiddle(final DefaultNode topNode, final Segment bottomSegment, final float landingOffsetX) {
		float landingX = topNode.x() + landingOffsetX;
		if (bottomSegment.containsX(landingX)) {
            DefaultNode bottomNode = new DefaultNode(landingX, bottomSegment.getY());
            bottomSegment.getNodes().add(bottomNode);
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
		float yOffset = endNode.y() - startNode.y();
		boolean canJumpToVertically = yOffset < jumpHeight;
		return canJumpToHorizontally && canJumpToVertically;
	}

	private void connectWithin(final Segment segment) {
        List<DefaultNode> nodes = new ArrayList<DefaultNode>(segment.getNodes());
        Collections.sort(nodes, new Comparator<DefaultNode>() {
			@Override
			public int compare(final DefaultNode n1, final DefaultNode n2) {
				return Float.compare(n1.x(), n2.x());
			}
		});
		for (int i = 0; i < nodes.size() - 1; i++) {
			DefaultNode node1 = nodes.get(i);
			DefaultNode node2 = nodes.get(i + 1);
			node1.addConnection(node2);
			node2.addConnection(node1);
		}
	}

}
