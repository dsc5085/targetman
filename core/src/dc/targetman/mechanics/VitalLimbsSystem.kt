package dc.targetman.mechanics

import dc.targetman.epf.parts.VitalLimbsPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.physics.limb.Limb
import dclib.physics.limb.LimbUtils

class VitalLimbsSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
	private val entityManager: EntityManager = entityManager

	override fun update(delta: Float, entity: Entity) {
		val vitalLimbsPart = entity.tryGet(VitalLimbsPart::class.java)
		val entities = entityManager.all
		val isVitalLimbDead = vitalLimbsPart?.limbs.orEmpty().any { LimbUtils.findEntity(entities, it) == null }
		if (isVitalLimbDead) {
			entityManager.remove(entity)
		}
	}
}