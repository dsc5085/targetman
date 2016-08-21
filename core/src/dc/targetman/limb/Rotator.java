package dc.targetman.limb;

import dclib.limb.Joint;
import dclib.util.FloatRange;

public final class Rotator {

	private final Joint joint;
	private final FloatRange range;
	private final float rotateSpeed;
	private float rotateMultiplier = 0;

	public Rotator(final Joint joint, final FloatRange range, final float rotateSpeed) {
		this.joint = joint;
		this.range = range;
		this.rotateSpeed = rotateSpeed;
	}

	public final void setRotateMultiplier(final float rotateMultiplier) {
		this.rotateMultiplier = rotateMultiplier;
	}

	public final void update(final float delta) {
		float rotation = joint.getRotation() + rotateSpeed * rotateMultiplier * delta;
		float clampedRotation = range.clamp(rotation);
		joint.setRotation(clampedRotation);
	}

}
