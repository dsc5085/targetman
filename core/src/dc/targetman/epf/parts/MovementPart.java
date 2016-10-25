package dc.targetman.epf.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dclib.physics.limb.Limb;

public final class MovementPart {

	private final float moveSpeed;
	private final float jumpForce;
	private final List<Limb> limbs;
	private int direction = 0;
	private boolean jumping = false;

	public MovementPart(final float moveSpeed, final float jumpForce, final Limb... limbs) {
		this.moveSpeed = moveSpeed;
		this.jumpForce = jumpForce;
		this.limbs = Arrays.asList(limbs);
	}

	public final float getMoveSpeed() {
		return moveSpeed;
	}

	public final float getJumpForce() {
		return jumpForce;
	}

	public final List<Limb> getLimbs() {
		return new ArrayList<Limb>(limbs);
	}

	public final int getDirection() {
		return direction;
	}

	public final void setDirection(final int direction) {
		this.direction = direction;
	}

	public final boolean jumping() {
		return jumping;
	}

	public final void setJumping(final boolean jumping) {
		this.jumping = jumping;
	}

}
