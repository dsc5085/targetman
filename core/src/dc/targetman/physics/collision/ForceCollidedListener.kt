package dc.targetman.physics.collision

import com.badlogic.gdx.math.Vector2
import com.google.common.base.Predicate
import dc.targetman.epf.parts.ForcePart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.TransformPart
import dclib.physics.Transform
import dclib.physics.collision.CollidedEvent
import dclib.physics.collision.CollidedListener
import dclib.physics.limb.LimbUtils

class ForceCollidedListener(entityManager: EntityManager, filter: Predicate<CollidedEvent>) : CollidedListener {
	private val entityManager: EntityManager = entityManager
	private val filter: Predicate<CollidedEvent> = filter

	override fun collided(event: CollidedEvent) {
		val sourceEntity = event.source.entity
		val forcePart = sourceEntity.tryGet(ForcePart::class.java)
		if (forcePart != null && filter.apply(event)) {
			val force = getForce(sourceEntity)
			applyForce(event.target.entity, force)
		}
	}

	private fun getForce(sourceEntity: Entity): Vector2 {
		val transform = sourceEntity[TransformPart::class.java].transform
		val force = sourceEntity[ForcePart::class.java].force
		return transform.velocity.setLength(force)
	}

	private fun applyForce(target: Entity, force: Vector2) {
		val actualTarget = LimbUtils.findContainer(entityManager.getAll(), target) ?: target
		val actualTransform = actualTarget[TransformPart::class.java].getTransform()
		actualTransform.applyImpulse(force)
	}
}