package dc.targetman.epf.parts;

import dclib.geometry.Centrum;
import dclib.limb.Joint;
import dclib.util.FloatRange;
import dclib.util.Timer;

public final class WeaponPart {

	private final String entityType;
	private final Centrum centrum;
	private final Timer fireTimer;
	private final Joint aimJoint;
	private final FloatRange aimRange;
	private boolean triggered = false;

	public WeaponPart(final String entityType, final Centrum centrum, final float fireTime,
			final Joint aimJoint, final FloatRange aimRange) {
		this.entityType = entityType;
		this.centrum = centrum;
		fireTimer = new Timer(fireTime);
		fireTimer.elapse();
		this.aimJoint = aimJoint;
		this.aimRange = aimRange;
	}

	public final String getEntityType() {
		return entityType;
	}

	public final Centrum getCentrum() {
		return centrum;
	}

	public final boolean shouldFire() {
		return triggered && fireTimer.isElapsed();
	}

	public final void rotateAimingLimb(final float rotationOffset) {
		float clampedRotation = aimRange.clamp(aimJoint.getRotation() + rotationOffset);
		aimJoint.setRotation(clampedRotation);
	}

	public final void reset() {
		fireTimer.reset();
	}

	public final void setTriggered(final boolean triggered) {
		this.triggered = triggered;
	}

	public final void update(final float delta) {
		fireTimer.tick(delta);
	}

}
