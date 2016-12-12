package dc.targetman.mechanics;

import dc.targetman.epf.parts.MovementPart;
import dc.targetman.epf.parts.WeaponPart;
import dclib.epf.Entity;

public final class StickActions {

	private StickActions() {
	}

	public static final void move(final Entity entity, final Direction direction) {
		entity.get(MovementPart.class).setDirection(direction);
	}

	public static final void jump(final Entity entity) {
		entity.get(MovementPart.class).setTryJumping(true);
	}

	public static final void aim(final Entity entity, final int direction) {
        // TODO:
//		entity.get(WeaponPart.class).setAimDirection(direction);
    }

	public static final void trigger(final Entity entity) {
		entity.get(WeaponPart.class).setTriggered(true);
	}

}
