package dc.targetman.physics.collision;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Predicate;

import dc.targetman.epf.parts.ForcePart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.TransformPart;
import dclib.physics.Transform;
import dclib.physics.collision.CollidedEvent;
import dclib.physics.collision.CollidedListener;
import dclib.physics.limb.LimbUtils;

public final class ForceCollidedListener implements CollidedListener {

	private final EntityManager entityManager;
	private final Predicate<CollidedEvent> filter;

	public ForceCollidedListener(final EntityManager entityManager, final Predicate<CollidedEvent> filter) {
		this.entityManager = entityManager;
		this.filter = filter;
	}

	@Override
	public final void collided(final CollidedEvent event) {
		Entity sourceEntity = event.getSource().getEntity();
		ForcePart forcePart = sourceEntity.tryGet(ForcePart.class);
		if (forcePart != null && filter.apply(event)) {
			Vector2 force = getForce(sourceEntity);
			applyForce(event.getTarget().getEntity(), force);
		}
	}

	private Vector2 getForce(final Entity sourceEntity) {
		Transform transform = sourceEntity.get(TransformPart.class).getTransform();
		float force = sourceEntity.get(ForcePart.class).getForce();
		return transform.getVelocity().setLength(force);
	}

	private void applyForce(final Entity target, final Vector2 force) {
		Entity actualTarget = LimbUtils.findContainer(entityManager.getAll(), target);
		if (actualTarget == null) {
			actualTarget = target;
		}
		Transform actualTransform = actualTarget.get(TransformPart.class).getTransform();
		actualTransform.applyImpulse(force);
	}

}
