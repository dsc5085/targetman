package dc.targetman.epf.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dc.targetman.level.EntityFactory;
import dc.targetman.level.models.CollisionType;
import dclib.epf.Entity;
import dclib.epf.parts.PhysicsPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.physics.CollidedListener;
import dclib.util.FloatRange;

public final class BleedCollidedListener implements CollidedListener {

	private final EntityFactory entityFactory;

	public BleedCollidedListener(final EntityFactory entityFactory) {
		this.entityFactory = entityFactory;
	}

	@Override
	public final void collided(final Entity collider, final Entity collidee, final Vector2 offset) {
		final int numParticles = 10;
		PhysicsPart colliderPhysicsPart = collider.get(PhysicsPart.class);
		PhysicsPart collideePhysicsPart = collidee.get(PhysicsPart.class);
		if (colliderPhysicsPart.containsAny(CollisionType.PROJECTILE)
				&& collideePhysicsPart.containsAny(CollisionType.FLESH)) {
			Vector2 spawnPosition = collider.get(TransformPart.class).getPosition();
			Vector2 spawnVelocity = collider.get(TranslatePart.class).getVelocity();
			for (int i = 0; i < numParticles; i++) {
				createBloodParticle(spawnPosition, spawnVelocity);
			}
		}
	}

	private void createBloodParticle(final Vector2 spawnPosition, final Vector2 spawnVelocity) {
		final FloatRange sizeRange = new FloatRange(0.01f, 0.07f);
		final FloatRange rotationDiffRange = new FloatRange(-10, 10);
		final FloatRange velocityRatioRange = new FloatRange(0.1f, 0.5f);
		Vector2 velocity = spawnVelocity.cpy();
		velocity.setAngle(velocity.angle() + rotationDiffRange.random());
		velocity.scl(velocityRatioRange.random());
		Vector3 position = new Vector3(spawnPosition.x, spawnPosition.y, 0);
		entityFactory.createBloodParticle(sizeRange.random(), position, velocity);
	}

}
