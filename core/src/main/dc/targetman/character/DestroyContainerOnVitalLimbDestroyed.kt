package dc.targetman.character

import dc.targetman.skeleton.LimbUtils
import dclib.epf.EntityDestroyedEvent
import dclib.epf.EntityManager

class DestroyContainerOnVitalLimbDestroyed(private val entityManager: EntityManager) : (EntityDestroyedEvent) -> Unit {
    override fun invoke(event: EntityDestroyedEvent) {
        val entity = event.entity
        if (entity.of(LimbAttribute.VITAL)) {
            val container = LimbUtils.findContainer(entityManager.getAll(), entity)
            if (container != null) {
                entityManager.destroy(container)
            }
        }
    }
}