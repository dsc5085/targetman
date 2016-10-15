package dc.targetman.ai;

import com.badlogic.gdx.math.Rectangle;

import dclib.geometry.RectangleUtils;
import dclib.physics.Box2dUtils;
import dclib.util.Maths;

public final class DefaultNode {

	private final Rectangle bounds;

	public DefaultNode(final Rectangle bounds) {
		this.bounds = bounds;
	}

	public final Rectangle getBounds() {
		return new Rectangle(bounds);
	}

	public final float left() {
		return bounds.x;
	}

	public final float right() {
		return RectangleUtils.right(bounds);
	}

	public final float top() {
		return RectangleUtils.top(bounds);
	}

	public final boolean isTouching(final Rectangle bounds) {
		Rectangle collisionBounds = new Rectangle(
				bounds.x - Box2dUtils.ROUNDING_ERROR, bounds.y - Box2dUtils.ROUNDING_ERROR,
				bounds.width + Box2dUtils.ROUNDING_ERROR * 2, bounds.height + Box2dUtils.ROUNDING_ERROR * 2);
		return collisionBounds.overlaps(this.bounds);
	}

	public final boolean canJumpTo(final float startX, final float startY) {
		// TODO: Replace these constants with calculations
		final float jumpHeight = 5;
		float yOffset = startY - top();
		return yOffset < jumpHeight && canJumpToHorizontally(startX);
	}

	@Override
	public final boolean equals(final Object o) {
		return o instanceof DefaultNode && ((DefaultNode)o).bounds.equals(bounds);
	}

	@Override
	public final String toString() {
		return bounds.toString();
	}

	private boolean canJumpToHorizontally(final float startX) {
		final float jumpWidth = 8;
		float gapWidth = Maths.min(Maths.distance(startX, left()), Maths.distance(startX, right()));
		return RectangleUtils.containsX(bounds, startX) || gapWidth < jumpWidth;
	}

}
