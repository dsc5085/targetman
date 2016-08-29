package dc.targetman.epf.systems;

import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.systems.EntitySystem;

public final class AiSystem extends EntitySystem {

	public AiSystem(final EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		// Aim at player
		// Fire if aimed
	}



}
