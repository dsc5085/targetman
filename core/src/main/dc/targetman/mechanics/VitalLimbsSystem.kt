package dc.targetman.mechanics

import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.VitalLimbsPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem

class VitalLimbsSystem(private val entityManager: EntityManager) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val vitalLimbsPart = entity.tryGet(VitalLimbsPart::class)
        if (vitalLimbsPart != null) {
            val skeletonPart = entity[SkeletonPart::class]
            val isVitalLimbDead = vitalLimbsPart.limbNames.any { !skeletonPart[it].isActive }
            if (isVitalLimbDead) {
                entityManager.remove(entity)
            }
        }
    }
}