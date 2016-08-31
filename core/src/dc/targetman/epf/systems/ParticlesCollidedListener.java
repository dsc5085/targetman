package dc.targetman.epf.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dc.targetman.level.EntityFactory;
import dc.targetman.level.models.CollisionType;
import dclib.epf.Entity;
import dclib.epf.parts.PhysicsPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.graphics.ParticlesManager;
import dclib.physics.CollidedListener;
import dclib.util.FloatRange;

public final class ParticlesCollidedListener implements CollidedListener {

	private final ParticlesManager particlesManager;
	private final EntityFactory entityFactory;

	public ParticlesCollidedListener(final ParticlesManager particlesManager, final EntityFactory entityFactory) {
		this.particlesManager = particlesManager;
		this.entityFactory = entityFactory;
	}

	@Override
	public final void collided(final Entity collider, final Entity collidee, final Vector2 offset) {
		PhysicsPart colliderPhysicsPart = collider.get(PhysicsPart.class);
		PhysicsPart collideePhysicsPart = collidee.get(PhysicsPart.class);
		Vector2 position = collider.get(TransformPart.class).getPosition();
		Vector2 velocity = collider.get(TranslatePart.class).getVelocity();
		if (colliderPhysicsPart.containsAny(CollisionType.METAL) && velocity.len() > 0) {
			if (collideePhysicsPart.containsAny(CollisionType.METAL)) {
				createSparks(position);
			} else if (collideePhysicsPart.containsAny(CollisionType.FLESH)) {
				createBloodParticles(position, velocity);
			}
		}
	}

	private void createSparks(final Vector2 position) {
		particlesManager.createEffect("spark", position);
	}

	private void createBloodParticles(final Vector2 position, final Vector2 velocity) {
		final float numParticles = 10;
		final FloatRange sizeRange = new FloatRange(0.01f, 0.07f);
		final FloatRange rotationDiffRange = new FloatRange(-10, 10);
		final FloatRange velocityRatioRange = new FloatRange(0.1f, 0.5f);
		for (int i = 0; i < numParticles; i++) {
			Vector2 randomizedVelocity = velocity.cpy();
			randomizedVelocity.setAngle(randomizedVelocity.angle() + rotationDiffRange.random());
			randomizedVelocity.scl(velocityRatioRange.random());
			Vector3 position3 = new Vector3(position.x, position.y, 0);
			entityFactory.createBloodParticle(sizeRange.random(), position3, randomizedVelocity);
		}
	}

}
