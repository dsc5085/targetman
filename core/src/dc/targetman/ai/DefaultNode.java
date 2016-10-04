package dc.targetman.ai;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dclib.geometry.RectangleUtils;
import dclib.physics.Box2dUtils;

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
		float yOffset = position.y - top();
		return position.x >= x() && position.x <= right() && yOffset >= 0 && yOffset < Box2dUtils.ROUNDING_ERROR;
	}

	public final boolean canJumpTo(final float x, final float right, final float y) {
		// TODO: Replace these constants with calculations
		final float jumpHeight = 5;
		float leftGap = x() - right;
		float rightGap = right() - x;
		float yOffset = y - top();
		return yOffset < jumpHeight && (canCrossGap(leftGap) || canCrossGap(rightGap));
	}

	@Override
	public final boolean equals(final Object o) {
		return o instanceof DefaultNode && ((DefaultNode)o).bounds.equals(bounds);
	}

	@Override
	public final String toString() {
		return bounds.toString();
	}

	private boolean canCrossGap(final float gap) {
		final float jumpDistance = 5;
		float gapDistance = Math.abs(gap);
		return gapDistance > 0 && gapDistance < jumpDistance;
	}

}
