package dc.targetman.physics.collision;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.SpritePart;
import dclib.epf.parts.TimedDeathPart;
import dclib.epf.parts.TransformPart;
import dclib.physics.DefaultTransform;
import dclib.physics.Transform;
import dclib.physics.collision.CollidedEvent;
import dclib.physics.collision.CollidedListener;
import dclib.util.FloatRange;

public final class StickyCollidedListener implements CollidedListener {

	private final EntityManager entityManager;

	public StickyCollidedListener(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public final void collided(final CollidedEvent event) {
		final FloatRange deathTimeRange = new FloatRange(10, 120);
		Entity sourceEntity = event.getSource().getEntity();
		BodyType targetBodyType = event.getTarget().getBody().getType();
		if (sourceEntity.of(Material.STICKY) && targetBodyType == BodyType.StaticBody) {
			Entity spawn = new Entity();
			Transform transform = sourceEntity.get(TransformPart.class).getTransform();
			Transform spawnTransform = new DefaultTransform(transform);
			// TODO:
//			Vector2 size = transform.getSize();
//			if (Math.abs(offset.x) < size.x || Math.abs(offset.y) < size.y) {
//				Vector2 stickOffset = new Vector2(size.x * -Math.signum(offset.x), size.y * -Math.signum(offset.y));
//				transform.translate(stickOffset);
//			}
			TimedDeathPart timedDeathPart = new TimedDeathPart(deathTimeRange.random());
			spawn.attach(new TransformPart(spawnTransform), sourceEntity.get(SpritePart.class), timedDeathPart);
			entityManager.add(spawn);
		}
	}

}
