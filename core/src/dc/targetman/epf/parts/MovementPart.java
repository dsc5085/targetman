package dc.targetman.epf.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dclib.physics.limb.Limb;

public final class MovementPart {

	private final float moveSpeed;
	private final float jumpSpeed;
	private final List<Limb> limbs;
	private float direction = 0;

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

	public final float getDirection() {
		return direction;
	}

	public final void setDirection(final float direction) {
		this.direction = direction;
	}

}
