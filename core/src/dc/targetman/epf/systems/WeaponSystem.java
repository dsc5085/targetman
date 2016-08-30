package dc.targetman.epf.systems;

import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.level.EntityFactory;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.systems.EntitySystem;

public final class WeaponSystem extends EntitySystem {

	private final EntityFactory entityFactory;

	public WeaponSystem(final EntityManager entityManager, final EntityFactory entityFactory) {
		super(entityManager);
		this.entityFactory = entityFactory;
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		WeaponPart weaponPart = entity.tryGet(WeaponPart.class);
		if (weaponPart != null) {
			fire(weaponPart);
			weaponPart.update(delta);
			weaponPart.setTriggered(false);
		}
	}

	private void fire(final WeaponPart weaponPart) {
		if (weaponPart.shouldFire()) {
			entityFactory.createBullet(weaponPart.getCentrum(), weaponPart.getEntityType());
			weaponPart.reset();
		}
	}

}
