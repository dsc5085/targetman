package dc.targetman.ai;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.math.Rectangle;

import dclib.geometry.RectangleUtils;
import dclib.util.Maths;

class Segment {

	Rectangle bounds;
	DefaultNode leftNode;
	DefaultNode rightNode;
	Set<DefaultNode> nodes = new HashSet<DefaultNode>();

	Segment(final Rectangle bounds) {
		this.bounds = bounds;
		leftNode = new DefaultNode(bounds.x, y());
		nodes.add(leftNode);
		rightNode = new DefaultNode(RectangleUtils.right(bounds), y());
		nodes.add(rightNode);
	}
	
	float left() {
		return leftNode.x();
	}
	
	float right() {
		return rightNode.x();
	}
	
	float y() {
		return RectangleUtils.top(bounds);
	}

	boolean containsX(final float x) {
		return Maths.between(x, left(), right());
	}

	boolean overlapsX(final Rectangle bounds) {
		float boundsRight = RectangleUtils.right(bounds);
		return Maths.between(left(), bounds.x, boundsRight)
				|| Maths.between(right(), bounds.x, boundsRight)
				|| Maths.between(bounds.x, left(), right())
				|| Maths.between(boundsRight, left(), right());
	}

}
