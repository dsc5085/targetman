package dc.targetman.epf.parts;

public final class MovementPart {

	private final float moveSpeed;
	private final float jumpSpeed;

	public MovementPart(final float moveSpeed, final float jumpSpeed) {
		this.moveSpeed = moveSpeed;
		this.jumpSpeed = jumpSpeed;
	}

	public final float getMoveSpeed() {
		return moveSpeed;
	}

	public final float getJumpSpeed() {
		return jumpSpeed;
	}

}
