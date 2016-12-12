package dc.targetman.mechanics

import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.VitalLimbsPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem

class VitalLimbsSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
	private val entityManager: EntityManager = entityManager

	// TODO: Subscribe to entity removed event instead of polling
	override fun update(delta: Float, entity: Entity) {
		val vitalLimbsPart = entity.tryGet(VitalLimbsPart::class.java)
        if (vitalLimbsPart != null) {
            val skeletonPart = entity[SkeletonPart::class.java]
            val isVitalLimbDead = vitalLimbsPart.limbNames.any { skeletonPart[it].isActive }
            if (isVitalLimbDead) {
                entityManager.remove(entity)
            }
		}
	}
}