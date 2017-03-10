package dc.targetman.skeleton

import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.eventing.EventDelegate

class LimbRemovedChecker(private val entityManager: EntityManager) {
    val limbRemoved = EventDelegate<LimbRemovedEvent>()

    private val ignoredLimbs = mutableListOf<Limb>()

    init {
        entityManager.entityRemoved.on { handleEntityRemoved(it.entity) }
    }

    private fun handleEntityRemoved(entity: Entity) {
        val limb = LimbUtils.find(entityManager.getAll(), entity)
        if (limb != null && !ignoredLimbs.remove(limb)) {
            limbRemoved.notify(LimbRemovedEvent(limb))
            val descendants = limb.getDescendants()
            ignoredLimbs.addAll(descendants)
            entityManager.removeAll(descendants.map { it.entity })
        }
    }
}