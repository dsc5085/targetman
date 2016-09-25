package dc.targetman.level;

import com.badlogic.gdx.math.Rectangle;

import dclib.geometry.RectangleUtils;

public final class DefaultNode {

	private final Rectangle bounds;

	public DefaultNode(final Rectangle bounds) {
		this.bounds = bounds;
	}

	// TODO: just return bounds?
	public final float height() {
		return bounds.height;
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

}
