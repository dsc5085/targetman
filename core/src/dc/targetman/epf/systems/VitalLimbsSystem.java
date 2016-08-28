package dc.targetman.epf.systems;

import java.util.List;

import dc.targetman.epf.parts.VitalLimbsPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.systems.EntitySystem;
import dclib.epf.util.LimbUtils;
import dclib.limb.Limb;

public final class VitalLimbsSystem extends EntitySystem {

	private final EntityManager entityManager;

	public VitalLimbsSystem(final EntityManager entityManager) {
		super(entityManager);
		this.entityManager = entityManager;
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		if (entity.has(VitalLimbsPart.class)) {
			List<Entity> entities = entityManager.getAll();
			Limb[] vitalLimbs = entity.get(VitalLimbsPart.class).getVitalLimbs();
			for (Limb limb : vitalLimbs) {
				if (LimbUtils.findEntity(entities, limb) == null) {
					entityManager.remove(entity);
					break;
				}
			}
		}
	}

}
