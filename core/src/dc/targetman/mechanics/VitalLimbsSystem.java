package dc.targetman.mechanics;

import java.util.List;

import dc.targetman.epf.parts.VitalLimbsPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystem;
import dclib.physics.limb.Limb;
import dclib.physics.limb.LimbUtils;

public final class VitalLimbsSystem extends EntitySystem {

	private final EntityManager entityManager;

	public VitalLimbsSystem(final EntityManager entityManager) {
		super(entityManager);
		this.entityManager = entityManager;
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		VitalLimbsPart vitalLimbsPart = entity.tryGet(VitalLimbsPart.class);
		if (vitalLimbsPart != null) {
			List<Entity> entities = entityManager.getAll();
			for (Limb limb : vitalLimbsPart.getVitalLimbs()) {
				if (LimbUtils.INSTANCE.findEntity(entities, limb) == null) {
					entityManager.remove(entity);
					break;
				}
			}
		}
	}

}
