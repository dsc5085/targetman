package dc.targetman.physics.collision;

import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.ForcePart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.TransformPart;
import dclib.physics.Contacter;
import dclib.physics.Transform;
import dclib.physics.collision.CollidedListener;
import dclib.physics.limb.LimbUtils;

public final class ForceCollidedListener implements CollidedListener {

	private final EntityManager entityManager;

	public ForceCollidedListener(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public final void collided(final Contacter collider, final Contacter collidee) {
		ForcePart forcePart = collider.getEntity().tryGet(ForcePart.class);
		if (forcePart != null && collidee.getEntity().is(forcePart.getCollisionGroup())) {
			Vector2 force = getForce(collider.getEntity());
			applyForce(collidee.getEntity(), force);
		}
	}

	private Vector2 getForce(final Entity collider) {
		Transform transform = collider.get(TransformPart.class).getTransform();
		float force = collider.get(ForcePart.class).getForce();
		return transform.getVelocity().setLength(force);
	}

	private void applyForce(final Entity collidee, final Vector2 force) {
		Entity actualCollidee = LimbUtils.findContainer(entityManager.getAll(), collidee);
		if (actualCollidee == null) {
			actualCollidee = collidee;
		}
		Transform actualTransform = actualCollidee.get(TransformPart.class).getTransform();
		actualTransform.applyImpulse(force);
	}

}
