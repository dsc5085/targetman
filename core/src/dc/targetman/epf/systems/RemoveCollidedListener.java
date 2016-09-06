package dc.targetman.epf.systems;


public final class RemoveCollidedListener {

	// TODO:
//	private final EntityManager entityManager;
//
//	public RemoveCollidedListener(final EntityManager entityManager) {
//		this.entityManager = entityManager;
//	}
//
//	@Override
//	public final void collided(final Entity collider, final Entity collidee, final Vector2 offsets) {
//		CollisionRemovePart collisionRemovePart = collider.tryGet(CollisionRemovePart.class);
//		PhysicsPart collideePhysicsPart = collidee.tryGet(PhysicsPart.class);
//		if (collisionRemovePart != null && collideePhysicsPart != null) {
//			List<Enum<?>> collisionGroups = collisionRemovePart.getCollisionGroups();
//			if (collideePhysicsPart.getBodyType() == BodyType.STATIC
//					|| collidee.get(CollisionPart.class).containsAny(collisionGroups)) {
//				entityManager.remove(collider);
//			}
//		}
//	}

}
