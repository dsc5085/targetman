package dc.targetman.ai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;

import dclib.geometry.RectangleUtils;
import dclib.util.Maths;

class Segment {

	// TODO: Don't need left, right node
	final Rectangle bounds;
	final float y;
	final DefaultNode leftNode;
	final DefaultNode rightNode;
	final List<DefaultNode> nodes = new ArrayList<DefaultNode>();

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
