package dc.targetman.mechanics;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import dc.targetman.epf.parts.MovementPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;

public final class EntityFinder {

	private EntityFinder() {
	}

	public static final Entity findPlayer(final EntityManager entityManager) {
		return Iterables.find(entityManager.getAll(), new Predicate<Entity>() {
			@Override
			public boolean apply(final Entity input) {
				return input.has(MovementPart.class) && input.of(Alliance.PLAYER);
			}
		});
	}

}
