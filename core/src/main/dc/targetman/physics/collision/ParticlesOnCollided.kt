package dc.targetman.physics.collision

import com.badlogic.gdx.graphics.g2d.ParticleEmitter
import com.badlogic.gdx.math.Vector2
import dc.targetman.mechanics.Alliance
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.graphics.TextureUtils
import dclib.physics.DefaultTransform
import dclib.physics.collision.CollidedEvent
import dclib.physics.particles.*
import dclib.util.FloatRange

class ParticlesOnCollided(
		private val entityManager: EntityManager,
		private val particlesManager: ParticlesManager
) : (CollidedEvent) -> Unit {
	override fun invoke(event: CollidedEvent) {
		val sourceEntity = event.source.entity
		val targetEntity = event.target.entity
		val velocity = event.source.body.linearVelocity
		if (sourceEntity.of(Material.METAL) && velocity.len() > 0) {
			createSparks(event)
			val targetAlliance = targetEntity.getAttribute(Alliance::class)
			if (targetAlliance != null && sourceEntity.of(targetAlliance.target) && targetEntity.of(Material.FLESH)) {
				createBloodParticles(targetEntity, velocity.angle())
			}
		}
	}

	private fun createSparks(event: CollidedEvent) {
		val target = event.target
        val targetAlliance = target.entity.getAttribute(Alliance::class)
        val notTargetAlliance = targetAlliance == null || !event.source.entity.of(targetAlliance)
		val contactPoint = event.contactPoint
        if (notTargetAlliance && target.entity.of(Material.METAL) && contactPoint != null) {
			particlesManager.createEffect("spark", StaticPositionGetter(contactPoint))
		}
	}

    private fun createBloodParticles(parentEntity: Entity, angle: Float) {
		val effect = particlesManager.createEffect("blood", EntityPositionGetter(parentEntity))
		for (emitter in effect.emitters) {
			val angleHighHalfDifference = (emitter.angle.highMax - emitter.angle.highMin) / 2
			emitter.angle.highMin = angle - angleHighHalfDifference
			emitter.angle.highMax = angle + angleHighHalfDifference
			val angleLowHalfDifference = (emitter.angle.lowMax - emitter.angle.lowMin) / 2
			emitter.angle.lowMin = angle - angleLowHalfDifference
			emitter.angle.lowMax = angle + angleLowHalfDifference
			if (emitter is ParticleEmitterBox2d) {
				emitter.particleCollidedDelegate.on(this::handleBloodParticleCollided)
			}
		}
	}

	private fun handleBloodParticleCollided(event: ParticleCollidedEvent) {
        createStain(event.particle, event.point)
	}

	private fun createStain(particle: ParticleEmitter.Particle, point: Vector2) {
		val stainScale = Vector2(2f, 0.5f)
		val deathTimeRange = FloatRange(10f, 120f)
		val stain = Entity()
		val size = Vector2(particle.width * particle.scaleX, particle.height * particle.scaleY)
		val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
		val transform = DefaultTransform(PolygonUtils.toPolygon(vertices), 5f)
		transform.rotation = particle.rotation
		val stainTransform = DefaultTransform(transform)
		stainTransform.setScale(stainTransform.scale.scl(stainScale))
		stainTransform.setWorld(stainTransform.center, point)
		val timedDeathPart = TimedDeathPart(deathTimeRange.random())
		val region = TextureUtils.createPolygonRegion(particle)
		stain.attach(TransformPart(stainTransform), SpritePart(region), timedDeathPart)
		entityManager.add(stain)
	}
}