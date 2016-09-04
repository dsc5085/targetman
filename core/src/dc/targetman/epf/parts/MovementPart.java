package dc.targetman.epf.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import dclib.limb.Limb;

public final class MovementPart {

	private final float moveSpeed;
	private final float jumpSpeed;
	private final List<Limb> limbs;
	private Vector2 velocity = new Vector2();

	public MovementPart(final float moveSpeed, final float jumpSpeed, final Limb... limbs) {
		this.moveSpeed = moveSpeed;
		this.jumpSpeed = jumpSpeed;
		this.limbs = Arrays.asList(limbs);
	}

	public final float getMoveSpeed() {
		return moveSpeed;
	}

	public final float getJumpSpeed() {
		return jumpSpeed;
	}

	public final List<Limb> getLimbs() {
		return new ArrayList<Limb>(limbs);
	}

	public final Vector2 getVelocity() {
		return velocity;
	}

	public final void setVelocity(final Vector2 velocity) {
		this.velocity = velocity;
	}

}
