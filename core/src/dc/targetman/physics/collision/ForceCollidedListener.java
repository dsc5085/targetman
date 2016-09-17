package dc.targetman.physics.collision;

import dclib.epf.EntityManager;
import dclib.physics.Contacter;
import dclib.physics.collision.CollidedListener;

public final class ForceCollidedListener implements CollidedListener {

//	private final EntityManager entityManager;

	public ForceCollidedListener(final EntityManager entityManager) {
//		this.entityManager = entityManager;
	}

	@Override
	public final void collided(final Contacter collider, final Contacter collidee) {
		// TODO:
//		ForcePart forcePart = collider.getEntity().tryGet(ForcePart.class);
//		CollisionPart collideePart = collidee.getEntity().tryGet(CollisionPart.class);
//		if (forcePart != null && collideePart != null && collideePart.containsAny(forcePart.getCollisionGroup())) {
//			Vector2 force = getForce(collider.getEntity());
//			applyForce(collidee.getEntity(), force);
//		}
	}

//	private Vector2 getForce(final Entity collider) {
//		TranslatePart translatePart = collider.get(TranslatePart.class);
//		float force = collider.get(ForcePart.class).getForce();
//		return translatePart.getVelocity().setLength(force);
//	}
//
//	private void applyForce(final Entity collidee, final Vector2 force) {
//		Transform transform = collidee.tryGet(TransformPart.class).getTransform();
//		Entity actualCollidee = LimbUtils.findContainer(entityManager.getAll(), transform);
//		if (actualCollidee == null) {
//			actualCollidee = collidee;
//		}
//		actualCollidee.get(TranslatePart.class).addVelocity(force);
//	}

}
