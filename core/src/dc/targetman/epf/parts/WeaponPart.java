package dc.targetman.epf.parts;

import dclib.geometry.Centrum;
import dclib.limb.Rotator;
import dclib.util.Timer;

public final class WeaponPart {

	private final String entityType;
	private final Centrum centrum;
	private final Timer fireTimer;
	private final Rotator aimRotator;
	private boolean triggered = false;

	public WeaponPart(final String entityType, final Centrum centrum, final float fireTime,
			final Rotator aimRotator) {
		this.entityType = entityType;
		this.centrum = centrum;
		fireTimer = new Timer(fireTime);
		fireTimer.elapse();
		this.aimRotator = aimRotator;
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

	public final void setRotateMultiplier(final float rotateMultiplier) {
		aimRotator.setRotateMultiplier(rotateMultiplier);
	}

	public final void reset() {
		fireTimer.reset();
	}

	public final void setTriggered(final boolean triggered) {
		this.triggered = triggered;
	}

	public final void update(final float delta) {
		fireTimer.tick(delta);
		aimRotator.update(delta);
	}

}
