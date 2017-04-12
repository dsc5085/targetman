package dc.targetman.physics.collision

import com.badlogic.gdx.graphics.g2d.ParticleEmitter
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dc.targetman.mechanics.Alliance
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.toVector2
import dclib.graphics.ScreenHelper
import dclib.graphics.TextureUtils
import dclib.physics.DefaultTransform
import dclib.physics.ParticleCollidedEvent
import dclib.physics.ParticleEmitterBox2d
import dclib.physics.ParticlesManager
import dclib.physics.collision.CollidedEvent
import dclib.util.FloatRange

class ParticlesOnCollided(
		private val screenHelper: ScreenHelper,
		private val entityManager: EntityManager,
		private val particlesManager: ParticlesManager
) : (CollidedEvent) -> Unit {
	override fun invoke(event: CollidedEvent) {
		val sourceEntity = event.source.entity
		val targetEntity = event.target.entity
        val position = sourceEntity[TransformPart::class].transform.position3
		position.z += MathUtils.FLOAT_ROUNDING_ERROR
		val velocity = event.source.body.linearVelocity
		if (sourceEntity.of(Material.METAL) && velocity.len() > 0) {
			createSparks(event)
			val targetAlliance = targetEntity.getAttribute(Alliance::class)
			if (targetAlliance != null && sourceEntity.of(targetAlliance.target) && targetEntity.of(Material.FLESH)) {
				createBloodParticles(position, velocity.angle())
			}
		}
	}

	private fun createSparks(event: CollidedEvent) {
		val target = event.target
        val targetAlliance = target.entity.getAttribute(Alliance::class)
        val notTargetAlliance = targetAlliance == null || !event.source.entity.of(targetAlliance)
		val contactPoint = event.contactPoint
        if (notTargetAlliance && !target.fixture.isSensor && target.entity.of(Material.METAL) && contactPoint != null) {
			particlesManager.createEffect("spark", contactPoint)
		}
	}

    private fun createBloodParticles(position: Vector3, angle: Float) {
		val effect = particlesManager.createEffect("blood", position.toVector2())
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
		val position = screenHelper.toWorldUnits(particle.x, particle.y)
		val size = screenHelper.toWorldUnits(particle.width, particle.height)
		val vertices = PolygonUtils.createRectangleVertices(Rectangle(position.x, position.y, size.x, size.y))
		val transform = DefaultTransform(PolygonUtils.toPolygon(vertices), 5f)
		transform.rotation = particle.rotation
		val stainTransform = DefaultTransform(transform)
		stainTransform.setScale(stainTransform.scale.scl(stainScale))
//		stainTransform.rotation = edgeContact.edge.angle
		stainTransform.setWorld(stainTransform.center, point)
		val timedDeathPart = TimedDeathPart(deathTimeRange.random())
		val region = TextureUtils.createPolygonRegion(particle)
//		val region = textureCache.getPolygonRegion("objects/blood")
		stain.attach(TransformPart(stainTransform), SpritePart(region), timedDeathPart)
		println(stainTransform.center)
		entityManager.add(stain)
	}
}