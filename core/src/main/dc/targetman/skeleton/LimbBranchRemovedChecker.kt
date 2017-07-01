package dc.targetman.skeleton

import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.eventing.EventDelegate

class LimbBranchRemovedChecker(private val entityManager: EntityManager) {
    val branchRemoved = EventDelegate<LimbBranchRemovedEvent>()

    init {
        entityManager.entityRemoved.on { handleEntityRemoved(it.entity) }
    }

    private fun handleEntityRemoved(entity: Entity) {
        val limb = LimbUtils.find(entityManager.getAll(), entity)
        if (limb != null) {
            val container = LimbUtils.findContainer(entityManager.getAll(), limb.entity)
            if (container != null) {
                val parentLimb = container[SkeletonPart::class].findParent(limb)
                if (parentLimb != null) {
                    branchRemoved.notify(LimbBranchRemovedEvent(limb))
                    parentLimb.detach(limb)
                    val branchDescendants = limb.getDescendants(includeLinked = true).minus(limb)
                    entityManager.removeAll(branchDescendants.map { it.entity })
                }
            }
        }
    }
}