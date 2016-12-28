package dc.targetman.skeleton

import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.eventing.EventDelegate

class LimbRemovedChecker(entityManager: EntityManager) {
    val limbRemoved = EventDelegate<LimbRemovedEvent>()

    private val entityManager = entityManager

    init {
        entityManager.entityRemoved.on { handleEntityRemoved(it.entity) }
    }

    private fun handleEntityRemoved(entity: Entity) {
        val limb = LimbUtils.find(entityManager.all, entity)
        if (limb != null) {
            limbRemoved.notify(LimbRemovedEvent(limb))
            val descendantEntities = limb.getDescendants().map { it.entity }
            entityManager.removeAll(descendantEntities)
        }
    }
}