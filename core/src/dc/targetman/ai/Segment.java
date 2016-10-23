package dc.targetman.ai;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.math.Rectangle;

import dclib.geometry.RectangleUtils;
import dclib.util.Maths;

class Segment {

	// TODO: Don't need left, right node.  Use x and right
	Rectangle bounds;
	float y;
	DefaultNode leftNode;
	DefaultNode rightNode;
	Set<DefaultNode> nodes = new HashSet<DefaultNode>();

	Segment(final Rectangle bounds) {
		this.bounds = bounds;
		y = RectangleUtils.top(bounds);
		leftNode = new DefaultNode(bounds.x, y);
		nodes.add(leftNode);
		rightNode = new DefaultNode(RectangleUtils.right(bounds), y);
		nodes.add(rightNode);
	}

	boolean containsX(final float x) {
		return Maths.between(x, leftNode.x(), rightNode.x());
	}

	boolean overlapsX(final Rectangle bounds) {
		float left = leftNode.x();
		float right = rightNode.x();
		float boundsRight = RectangleUtils.right(bounds);
		return Maths.between(left, bounds.x, boundsRight)
				|| Maths.between(right, bounds.x, boundsRight)
				|| Maths.between(bounds.x, left, right)
				|| Maths.between(boundsRight, left, right);
	}

}
