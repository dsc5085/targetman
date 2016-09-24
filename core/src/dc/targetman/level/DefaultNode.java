package dc.targetman.level;

import com.badlogic.gdx.math.Vector2;

public final class DefaultNode {

	private final Vector2 position;
	private final int length;

	public DefaultNode(final Vector2 position, final int length) {
		this.position = position;
		this.length = length;
	}

	public final float getLeft() {
		return position.x;
	}

	public final float getRight() {
		return position.x + length;
	}

	public final float getY() {
		return position.y;
	}

	public final float getLength() {
		return length;
	}

}
