package dc.targetman.physics.collision;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dc.targetman.level.EntityFactory;
import dc.targetman.mechanics.Alliance;
import dclib.epf.Entity;
import dclib.epf.parts.TransformPart;
import dclib.physics.Contacter;
import dclib.physics.ParticlesManager;
import dclib.physics.collision.CollidedEvent;
import dclib.physics.collision.CollidedListener;
import dclib.util.FloatRange;

public final class ParticlesCollidedListener implements CollidedListener {

	private final ParticlesManager particlesManager;
	private final EntityFactory entityFactory;

	public ParticlesCollidedListener(final ParticlesManager particlesManager, final EntityFactory entityFactory) {
		this.particlesManager = particlesManager;
		this.entityFactory = entityFactory;
	}

	@Override
	public final void collided(final CollidedEvent event) {
		Entity sourceEntity = event.getSource().getEntity();
		Entity targetEntity = event.getTarget().getEntity();
		Vector3 position = sourceEntity.get(TransformPart.class).getTransform().getPosition3();
		Vector2 velocity = event.getSource().getBody().getLinearVelocity();
		if (sourceEntity.of(Material.METAL) && velocity.len() > 0) {
			createSparks(sourceEntity, event.getTarget(), position);
			createBloodParticles(sourceEntity, targetEntity, position, velocity);
		}
	}

	private void createSparks(final Entity sourceEntity, final Contacter target, final Vector3 position) {
		Alliance targetAlliance = getAttribute(target.getEntity(), Alliance.class);
		if (!sourceEntity.of(targetAlliance) && !target.getFixture().isSensor()
				&& target.getEntity().of(Material.METAL)) {
			particlesManager.createEffect("spark", new Vector2(position.x, position.y));
		}
	}

	private void createBloodParticles(final Entity sourceEntity, final Entity targetEntity, final Vector3 position,
			final Vector2 velocity) {
		Alliance targetAlliance = getAttribute(targetEntity, Alliance.class);
		if (targetAlliance != null && sourceEntity.of(targetAlliance.getTarget()) && targetEntity.of(Material.FLESH)) {
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
