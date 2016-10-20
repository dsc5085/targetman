package dc.targetman.ai;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.math.Rectangle;

import dclib.geometry.RectangleUtils;
import dclib.util.Maths;

class Segment {

	// TODO: Don't need left, right node.  Use x and right
	final Rectangle bounds;
	final float y;
	final DefaultNode leftNode;
	final DefaultNode rightNode;
	final Set<DefaultNode> nodes = new HashSet<DefaultNode>();

	Segment(final Rectangle bounds) {
		this.bounds = bounds;
		y = RectangleUtils.top(bounds);
		leftNode = new DefaultNode(bounds.x, y);
		nodes.add(leftNode);
		rightNode = new DefaultNode(RectangleUtils.right(bounds), y);
		nodes.add(rightNode);
	}

	final boolean containsX(final float x) {
		return Maths.between(x, leftNode.x(), rightNode.x());
	}

}
