package dc.targetman.physics.collision

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dc.targetman.level.EntityFactory
import dc.targetman.mechanics.EntityUtils
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.physics.Contacter
import dclib.physics.ParticlesManager
import dclib.physics.collision.CollidedEvent
import dclib.util.FloatRange

class ParticlesOnCollided(val particlesManager: ParticlesManager, val entityFactory: EntityFactory)
    : (CollidedEvent) -> Unit {
	override fun invoke(event: CollidedEvent) {
		val sourceEntity = event.source.entity
		val targetEntity = event.target.entity
        val position = sourceEntity[TransformPart::class].transform.position3
		position.z += MathUtils.FLOAT_ROUNDING_ERROR
		val velocity = event.source.body.linearVelocity
		if (sourceEntity.of(Material.METAL) && velocity.len() > 0) {
			createSparks(sourceEntity, event.target, position)
			createBloodParticles(sourceEntity, targetEntity, position, velocity)
		}
	}

	private fun createSparks(sourceEntity: Entity, target: Contacter, position: Vector3) {
        val targetAlliance = EntityUtils.getAlliance(target.entity)
        val notTargetAlliance = targetAlliance != null && !sourceEntity.of(targetAlliance)
        if (notTargetAlliance && !target.fixture.isSensor && target.entity.of(Material.METAL)) {
			particlesManager.createEffect("spark", Vector2(position.x, position.y))
		}
	}

    private fun createBloodParticles(
            sourceEntity: Entity,
            targetEntity: Entity,
            position: Vector3,
            velocity: Vector2) {
		val numParticles = 10
		val sizeRange = FloatRange(0.01f, 0.07f)
		val rotationDiffRange = FloatRange(-10f, 10f)
		val velocityRatioRange = FloatRange(0.1f, 0.5f)
		// TODO: use filterIsInstanceTo
        val targetAlliance = EntityUtils.getAlliance(targetEntity)
		if (targetAlliance != null && sourceEntity.of(targetAlliance.target) && targetEntity.of(Material.FLESH)) {
			for (i in 0..numParticles)  {
				val randomizedVelocity = velocity.cpy()
				randomizedVelocity.setAngle(randomizedVelocity.angle() + rotationDiffRange.random())
				randomizedVelocity.scl(velocityRatioRange.random())
				entityFactory.createBloodParticle(sizeRange.random(), position, randomizedVelocity)
			}
		}
	}
}