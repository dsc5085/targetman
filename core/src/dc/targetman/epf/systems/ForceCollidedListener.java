package dc.targetman.epf.systems;

import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.ForcePart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.CollisionPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.epf.util.LimbUtils;
import dclib.physics.CollidedListener;

public final class ForceCollidedListener implements CollidedListener {

	private final EntityManager entityManager;

	public ForceCollidedListener(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public final void collided(final Entity collider, final Entity collidee, final Vector2 offset) {
		ForcePart forcePart = collider.tryGet(ForcePart.class);
		CollisionPart collideePart = collidee.tryGet(CollisionPart.class);
		if (forcePart != null && collideePart != null && collideePart.containsAny(forcePart.getCollisionGroup())) {
			Vector2 force = getForce(collider);
			applyForce(collidee, force);
		}
	}

	private Vector2 getForce(final Entity collider) {
		TranslatePart translatePart = collider.get(TranslatePart.class);
		float force = collider.get(ForcePart.class).getForce();
		return translatePart.getVelocity().setLength(force);
	}

	private void applyForce(final Entity collidee, final Vector2 force) {
		TransformPart collideeTransformPart = collidee.tryGet(TransformPart.class);
		Entity actualCollidee = LimbUtils.findContainer(entityManager.getAll(), collideeTransformPart.getPolygon());
		if (actualCollidee == null) {
			actualCollidee = collidee;
		}
		actualCollidee.get(TranslatePart.class).addVelocity(force);
	}

}
