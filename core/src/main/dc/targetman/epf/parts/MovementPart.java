package dc.targetman.epf.parts;

import dc.targetman.mechanics.Direction;
import dclib.physics.limb.Limb;
import dclib.util.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MovementPart {

    private final float MAX_JUMP_INCREASE_TIME = 0.2f;

	private final float moveSpeed;
    private final float jumpHeight;
    private final List<Limb> limbs;
	private Direction direction = Direction.NONE;
	private boolean isJumping = false;
    private final Timer jumpIncreaseTimer = new Timer(MAX_JUMP_INCREASE_TIME);

    public MovementPart(final float moveSpeed, final float jumpHeight, final Limb... limbs) {
        this.moveSpeed = moveSpeed;
        this.jumpHeight = jumpHeight;
        this.limbs = Arrays.asList(limbs);
	}

	public final float getMoveSpeed() {
		return moveSpeed;
	}

    /**
     * @return max jump height possible.  The actual value observed in the simulation may be greater
     */
    public final float getJumpHeight() {
        return jumpHeight;
    }

	public final List<Limb> getLimbs() {
		return new ArrayList<Limb>(limbs);
	}

	public final Direction getDirection() {
		return direction;
	}

	public final void setDirection(final Direction direction) {
		this.direction = direction;
	}

	public final boolean isJumping() {
		return isJumping;
	}

    public final Timer getJumpIncreaseTimer() {
        return jumpIncreaseTimer;
    }

	public final void setJumping(final boolean isJumping) {
		this.isJumping = isJumping;
	}

}
