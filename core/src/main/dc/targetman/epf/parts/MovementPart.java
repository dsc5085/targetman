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
    private final float jumpSpeed;
    private final List<Limb> limbs;
	private Direction direction = Direction.NONE;
	private boolean isJumping = false;
    private final Timer jumpIncreaseTimer = new Timer(MAX_JUMP_INCREASE_TIME);

    public MovementPart(final float moveSpeed, final float jumpSpeed, final Limb... limbs) {
        this.moveSpeed = moveSpeed;
        this.jumpSpeed = jumpSpeed;
        this.limbs = Arrays.asList(limbs);
	}

	public final float getMoveSpeed() {
		return moveSpeed;
	}

    /**
     * @return jump apex height
     */
    public final float getJumpSpeed() {
        return jumpSpeed;
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

    public final boolean getTryJumping() {
        return isJumping;
	}

    public final Timer getJumpIncreaseTimer() {
        return jumpIncreaseTimer;
    }

    public final void setTryJumping(final boolean isJumping) {
        this.isJumping = isJumping;
	}

}
