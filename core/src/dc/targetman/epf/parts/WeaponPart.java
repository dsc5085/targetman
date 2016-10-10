package dc.targetman.epf.parts;

import dc.targetman.mechanics.Weapon;
import dclib.geometry.Centrum;
import dclib.physics.limb.Limb;
import dclib.physics.limb.Rotator;
import dclib.util.Timer;

public final class WeaponPart {

	private final Centrum centrum;
	private final Timer reloadTimer;
	private final Weapon weapon;
	private final Rotator aimRotator;
	private boolean triggered = false;

	public WeaponPart(final Centrum centrum, final Weapon weapon, final Rotator aimRotator) {
		this.centrum = centrum;
		reloadTimer = new Timer(weapon.getReloadTime(), weapon.getReloadTime());
		this.weapon = weapon;
		this.aimRotator = aimRotator;
	}

	public final Weapon getWeapon() {
		return weapon;
	}

	public final Limb getRotatorLimb() {
		return aimRotator.getJoint().getLimb();
	}

	public final Centrum getCentrum() {
		return centrum;
	}

	public final boolean shouldFire() {
		return triggered && reloadTimer.isElapsed();
	}

	public final void setAimDirection(final float aimDirection) {
		aimRotator.setRotateMultiplier(aimDirection);
	}

	public final void reset() {
		reloadTimer.reset();
	}

	public final void setTriggered(final boolean triggered) {
		this.triggered = triggered;
	}

	public final void update(final float delta) {
		reloadTimer.tick(delta);
		aimRotator.update(delta);
	}

}
