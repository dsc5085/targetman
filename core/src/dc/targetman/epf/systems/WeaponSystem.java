package dc.targetman.epf.systems;

import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.level.EntityFactory;
import dclib.epf.Entity;
import dclib.epf.EntitySystem;

public final class WeaponSystem extends EntitySystem {

	private final EntityFactory entityFactory;

	public WeaponSystem(final EntityFactory entityFactory) {
		this.entityFactory = entityFactory;
	}

	@Override
	public final void update(final float delta, final Entity entity) {
		if (entity.hasActive(WeaponPart.class)) {
			WeaponPart weaponPart = entity.get(WeaponPart.class);
			fire(weaponPart);
			weaponPart.update(delta);
			weaponPart.setTriggered(false);
		}
	}

	private void fire(final WeaponPart weaponPart) {
		if (weaponPart.shouldFire()) {
			entityFactory.createBullet(weaponPart.getCentrum());
			weaponPart.reset();
		}
	}

}
