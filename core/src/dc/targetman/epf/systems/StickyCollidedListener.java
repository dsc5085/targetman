package dc.targetman.epf.systems;

import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.StickyPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.DrawablePart;
import dclib.epf.parts.PhysicsPart;
import dclib.epf.parts.TimedDeathPart;
import dclib.epf.parts.TransformPart;
import dclib.physics.BodyType;
import dclib.physics.CollidedListener;
import dclib.util.FloatRange;

public final class StickyCollidedListener implements CollidedListener {

	private final EntityManager entityManager;

	public StickyCollidedListener(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public final void collided(final Entity collider, final Entity collidee, final Vector2 offset) {
		final FloatRange deathTimeRange = new FloatRange(10, 120);
		PhysicsPart collideePhysicsPart = collidee.tryGet(PhysicsPart.class);
		if (collider.has(StickyPart.class) && collideePhysicsPart != null
				&& collideePhysicsPart.getBodyType() == BodyType.STATIC) {
			Entity spawn = new Entity();
			TransformPart transformPart = collider.get(TransformPart.class);
			Vector2 size = transformPart.getBounds().getSize(new Vector2());
			if (Math.abs(offset.x) < size.x || Math.abs(offset.y) < size.y) {
				Vector2 stickOffset = new Vector2(size.x * -Math.signum(offset.x), size.y * -Math.signum(offset.y));
				transformPart.translate(stickOffset);
			}
			TimedDeathPart timedDeathPart = new TimedDeathPart(deathTimeRange.random());
			spawn.attach(transformPart, collider.get(DrawablePart.class), timedDeathPart);
			entityManager.add(spawn);
		}
	}

}
