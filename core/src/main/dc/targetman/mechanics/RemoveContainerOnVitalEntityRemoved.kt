package dc.targetman.mechanics

import dc.targetman.epf.parts.VitalLimbsPart
import dc.targetman.skeleton.LimbUtils
import dclib.epf.EntityManager
import dclib.epf.EntityRemovedEvent

class RemoveContainerOnVitalEntityRemoved(private val entityManager: EntityManager) : (EntityRemovedEvent) -> Unit {
    override fun invoke(event: EntityRemovedEvent) {
        val limb = LimbUtils.find(entityManager.all, event.entity)
        if (limb != null) {
            val vitalLimbsPart = limb.container.tryGet(VitalLimbsPart::class)
            if (vitalLimbsPart != null) {
                if (vitalLimbsPart.limbNames.contains(limb.name)) {
                    entityManager.remove(limb.container)
                }
            }
        }
    }
}