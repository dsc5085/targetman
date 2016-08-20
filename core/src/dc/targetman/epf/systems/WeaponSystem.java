package dc.targetman.epf.systems;

import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.level.EntityFactory;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystem;
import dclib.geometry.Centrum;

public final class WeaponSystem extends EntitySystem {

	private final EntityManager entityManager;
	private final EntityFactory entityFactory;

	public WeaponSystem(final EntityManager entityManager, final EntityFactory entityFactory) {
		this.entityManager = entityManager;
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
			Centrum centrum = weaponPart.getCentrum();
			Entity bullet = entityFactory.createBullet(centrum.getPosition(), centrum.getRotation());
			entityManager.add(bullet);
			weaponPart.reset();
		}
	}

}
