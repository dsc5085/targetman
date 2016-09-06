package dc.targetman.epf.systems;


public final class StickyCollidedListener {

	// TODO:
//	private final EntityManager entityManager;
//
//	public StickyCollidedListener(final EntityManager entityManager) {
//		this.entityManager = entityManager;
//	}
//
//	@Override
//	public final void collided(final Entity collider, final Entity collidee, final Vector2 offset) {
//		final FloatRange deathTimeRange = new FloatRange(10, 120);
//		PhysicsPart collideePhysicsPart = collidee.tryGet(PhysicsPart.class);
//		if (collider.has(StickyPart.class) && collideePhysicsPart != null
//				&& collideePhysicsPart.getBodyType() == BodyType.STATIC) {
//			Entity spawn = new Entity();
//			TransformPart transformPart = collider.get(TransformPart.class);
//			Vector2 size = transformPart.getBounds().getSize(new Vector2());
//			if (Math.abs(offset.x) < size.x || Math.abs(offset.y) < size.y) {
//				Vector2 stickOffset = new Vector2(size.x * -Math.signum(offset.x), size.y * -Math.signum(offset.y));
//				transformPart.translate(stickOffset);
//			}
//			TimedDeathPart timedDeathPart = new TimedDeathPart(deathTimeRange.random());
//			spawn.attach(transformPart, collider.get(DrawablePart.class), timedDeathPart);
//			entityManager.add(spawn);
//		}
//	}

}
