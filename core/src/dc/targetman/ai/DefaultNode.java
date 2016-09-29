package dc.targetman.ai;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dclib.geometry.RectangleUtils;
import dclib.util.Maths;

public final class DefaultNode {

	private final Rectangle bounds;

	public DefaultNode(final Rectangle bounds) {
		this.bounds = bounds;
	}

	public final float x() {
		return bounds.x;
	}

	public final float right() {
		return RectangleUtils.right(bounds);
	}

	public final float top() {
		return RectangleUtils.top(bounds);
	}

	public final boolean at(final Vector2 position) {
		float yDistance = Maths.distance(position.y, top());
		return position.x >= x() && position.x <= right() && yDistance < bounds.height;
	}

	@Override
	public final boolean equals(final Object o) {
		return o instanceof DefaultNode && ((DefaultNode)o).bounds.equals(bounds);
	}

	@Override
	public final String toString() {
		return bounds.toString();
	}

}
