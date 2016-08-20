package dc.targetman.epf.parts;

import dclib.geometry.Centrum;
import dclib.util.Timer;

public final class WeaponPart {

	private final String entityType;
	private final Centrum centrum;
	private final Timer fireTimer;
	private boolean triggered = false;

	public WeaponPart(final String entityType, final Centrum centrum, final float fireTime) {
		this.entityType = entityType;
		this.centrum = centrum;
		fireTimer = new Timer(fireTime);
		fireTimer.elapse();
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
