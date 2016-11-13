package dc.targetman.physics.limb;

import dclib.physics.limb.Joint;
import dclib.physics.limb.LimbAnimation;
import dclib.util.FloatRange;

public final class WalkAnimation extends LimbAnimation {

	private static final float ROTATION_SPEED = 65;

	private final Joint leftLegJoint;
	private final Joint rightLegJoint;
	private final FloatRange rotationRange;
	private int direction = 1;

	public WalkAnimation(final Joint leftLegJoint, final Joint rightLegJoint, final FloatRange rotationRange) {
		this.leftLegJoint = leftLegJoint;
		this.rightLegJoint = rightLegJoint;
		this.rotationRange = rotationRange;
	}

	@Override
	public final void updateAnimation(final float delta) {
		float leftLegRotation = rotationRange.clamp(leftLegJoint.getRotation() + ROTATION_SPEED * direction * delta);
		leftLegJoint.setRotation(leftLegRotation);
		float leftLegDifference = leftLegRotation - rotationRange.min();
		rightLegJoint.setRotation(rotationRange.max() - leftLegDifference);
		boolean reverseDirection = direction == 1
				? leftLegRotation >= rotationRange.max()
				: leftLegRotation <= rotationRange.min();
		if (reverseDirection) {
			direction *= -1;
		}
	}

}
