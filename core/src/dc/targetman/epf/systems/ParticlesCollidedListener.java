package dc.targetman.epf.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dc.targetman.level.EntityFactory;
import dc.targetman.level.models.Alliance;
import dc.targetman.level.models.CollisionType;
import dclib.epf.Entity;
import dclib.epf.parts.TransformPart;
import dclib.graphics.ParticlesManager;
import dclib.physics.CollidedListener;
import dclib.physics.Contacter;
import dclib.util.FloatRange;

public final class ParticlesCollidedListener implements CollidedListener {

	private final ParticlesManager particlesManager;
	private final EntityFactory entityFactory;

	public ParticlesCollidedListener(final ParticlesManager particlesManager, final EntityFactory entityFactory) {
		this.particlesManager = particlesManager;
		this.entityFactory = entityFactory;
	}

	@Override
	public final void collided(final Contacter collider, final Contacter collidee) {
		Entity colliderEntity = collider.getEntity();
		Entity collideeEntity = collidee.getEntity();
		Vector3 position = collider.getEntity().get(TransformPart.class).getTransform().getPosition3();
		Vector2 velocity = collider.getBody().getLinearVelocity();
		if (colliderEntity.is(CollisionType.METAL) && velocity.len() > 0) {
			createSparks(colliderEntity, collideeEntity, position);
			createBloodParticles(colliderEntity, collideeEntity, position, velocity);
		}
	}

	private void createSparks(final Entity collider, final Entity collidee, final Vector3 position) {
		Alliance collideeAlliance = getAttribute(collidee, Alliance.class);
		if (!collider.is(collideeAlliance) && collidee.is(CollisionType.METAL)) {
			particlesManager.createEffect("spark", new Vector2(position.x, position.y));
		}
	}

	private void createBloodParticles(final Entity collider, final Entity collidee, final Vector3 position, 
			final Vector2 velocity) {
		Alliance collideeAlliance = getAttribute(collidee, Alliance.class);
		if (collideeAlliance != null && collider.is(collideeAlliance.getTarget()) && collidee.is(CollisionType.FLESH)) {
			final float numParticles = 10;
			final FloatRange sizeRange = new FloatRange(0.01f, 0.07f);
			final FloatRange rotationDiffRange = new FloatRange(-10, 10);
			final FloatRange velocityRatioRange = new FloatRange(0.1f, 0.5f);
			for (int i = 0; i < numParticles; i++) {
				Vector2 randomizedVelocity = velocity.cpy();
				randomizedVelocity.setAngle(randomizedVelocity.angle() + rotationDiffRange.random());
				randomizedVelocity.scl(velocityRatioRange.random());
				position.z += MathUtils.FLOAT_ROUNDING_ERROR;
				entityFactory.createBloodParticle(sizeRange.random(), position, randomizedVelocity);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private final <T extends Enum<T>> T getAttribute(final Entity entity, final Class<T> attributeClass) {
		for (Enum<?> attribute : entity.getAttributes()) {
			if (attribute.getClass() == attributeClass) {
				return (T)attribute;
			}
		}
		return null;
	}

}
