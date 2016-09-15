package dc.targetman.epf.systems;

import dclib.epf.EntityManager;
import dclib.physics.CollidedListener;
import dclib.physics.Contacter;

public final class StickyCollidedListener implements CollidedListener {

	// TODO:
	private final EntityManager entityManager;

	public StickyCollidedListener(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public final void collided(final Contacter collider, final Contacter collidee) {
//		final FloatRange deathTimeRange = new FloatRange(10, 120);
//		Entity colliderEntity = collider.getEntity();
//		if (colliderEntity.has(StickyPart.class) && collidee.getBody().getType() == BodyType.StaticBody) {
//			Entity spawn = new Entity();
//			Transform transform = colliderEntity.get(TransformPart.class).getTransform();
//			Vector2 size = transform.getSize();
////			if (Math.abs(offset.x) < size.x || Math.abs(offset.y) < size.y) {
////				Vector2 stickOffset = new Vector2(size.x * -Math.signum(offset.x), size.y * -Math.signum(offset.y));
////				transform.translate(stickOffset);
////			}
//			TimedDeathPart timedDeathPart = new TimedDeathPart(deathTimeRange.random());
//			spawn.attach(new TransformPart(transform), colliderEntity.get(DrawablePart.class), timedDeathPart);
//			entityManager.add(spawn);
//		}
	}

}
