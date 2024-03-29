package dc.targetman.physics.collision

import com.badlogic.gdx.math.Vector2
import com.google.common.base.Predicate
import dc.targetman.epf.parts.ForcePart
import dc.targetman.physics.PhysicsUtils
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.TransformPart
import dclib.physics.collision.CollidedEvent

class ForceOnCollided(val entityManager: EntityManager, val filter: Predicate<CollidedEvent>)
 : (CollidedEvent) -> Unit {
	override fun invoke(event: CollidedEvent) {
		val sourceEntity = event.collision.source.entity
        val forcePart = sourceEntity.tryGet(ForcePart::class)
		if (forcePart != null && filter.apply(event)) {
			val force = getForce(sourceEntity)
			PhysicsUtils.applyForce(entityManager.getAll(), event.collision.target.entity, force)
		}
	}

	private fun getForce(sourceEntity: Entity): Vector2 {
        val transform = sourceEntity[TransformPart::class].transform
		val forcePart = sourceEntity[ForcePart::class]
		return transform.velocity.setLength(forcePart.force)
	}
}