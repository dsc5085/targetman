package dc.targetman.ai;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dclib.physics.Box2dUtils;

public final class DefaultNode {

	private final Vector2 position;

	public DefaultNode(final float x, final float y) {
		position = new Vector2(x, y);
	}

	public final float x() {
		return position.x;
	}

	public final float y() {
		return position.y;
	}

	public final boolean isTouching(final Rectangle bounds) {
		Rectangle collisionBounds = new Rectangle(
				bounds.x - Box2dUtils.ROUNDING_ERROR, bounds.y - Box2dUtils.ROUNDING_ERROR,
				bounds.width + Box2dUtils.ROUNDING_ERROR * 2, bounds.height + Box2dUtils.ROUNDING_ERROR * 2);
		return collisionBounds.contains(position);
	}

	@Override
	public final boolean equals(final Object o) {
		return o instanceof DefaultNode && ((DefaultNode)o).position.equals(position);
	}

	@Override
	public final String toString() {
		return position.toString();
	}

}
