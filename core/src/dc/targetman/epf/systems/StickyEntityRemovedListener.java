package dc.targetman.epf.systems;

import dc.targetman.epf.parts.StickyPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntityRemovedListener;
import dclib.epf.parts.DrawablePart;
import dclib.epf.parts.TransformPart;

public final class StickyEntityRemovedListener implements EntityRemovedListener {

	private final EntityManager entityManager;

	public StickyEntityRemovedListener(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public final void removed(final Entity entity) {
		if (entity.has(StickyPart.class)) {
			Entity spawn = new Entity();
			spawn.attach(entity.get(TransformPart.class));
			spawn.attach(entity.get(DrawablePart.class));
			entityManager.add(spawn);
		}
	}

}
