package dc.targetman.skeleton

import dc.targetman.epf.parts.SkeletonPart
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
        val container = LimbUtils.findContainer(entityManager.all, entity)
        val skeletonPart = container?.tryGet(SkeletonPart::class.java)
        if (skeletonPart != null) {
            limbRemoved.notify(LimbRemovedEvent(entity, container!!))
            val name = skeletonPart.getName(entity)
            val descendantEntities = skeletonPart.getDescendants(name)
            entityManager.removeAll(descendantEntities)
        }
    }
}