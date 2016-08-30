package dc.targetman.epf.systems;

import java.util.List;

import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.level.EntityFactory;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.LimbsPart;
import dclib.epf.systems.EntitySystem;
import dclib.limb.Limb;
import dclib.util.CollectionUtils;

public final class WeaponSystem extends EntitySystem {

	private final EntityFactory entityFactory;

	public WeaponSystem(final EntityManager entityManager, final EntityFactory entityFactory) {
		super(entityManager);
		this.entityFactory = entityFactory;
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		WeaponPart weaponPart = entity.tryGet(WeaponPart.class);
		if (weaponPart != null && hasFiringLimbs(entity)) {
			fire(weaponPart);
			weaponPart.update(delta);
			weaponPart.setTriggered(false);
		}
	}

	private boolean hasFiringLimbs(final Entity entity) {
		WeaponPart weaponPart = entity.tryGet(WeaponPart.class);
		List<Limb> limbs = entity.get(LimbsPart.class).getRoot().getDescendants();
		List<Limb> firingLimbs = weaponPart.getRotatorLimb().getDescendants();
		return CollectionUtils.containsAll(limbs, firingLimbs);
	}

	private void fire(final WeaponPart weaponPart) {
		if (weaponPart.shouldFire()) {
			entityFactory.createBullet(weaponPart.getCentrum(), weaponPart.getEntityType());
			weaponPart.reset();
		}
	}

}
