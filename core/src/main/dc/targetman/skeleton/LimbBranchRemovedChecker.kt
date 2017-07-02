package dc.targetman.skeleton

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
            // TODO: Remove the need for skeletonroot entities to need a SkeletonPart
            val container = LimbUtils.findContainer(entityManager.getAll(), entity)
            if (limb.bone === limb.skeleton.rootBone && container != null) {
                entityManager.remove(container)
            }
            destroyBranch(limb)
            val parentLimb = LimbUtils.findParent(entityManager.getAll(), limb)
            if (parentLimb != null) {
                parentLimb.detach(limb)
            }
        }
    }

    private fun destroyBranch(limb: Limb) {
        branchRemoved.notify(LimbBranchRemovedEvent(limb))
        val branchDescendants = limb.getDescendants(includeLinked = true).minus(limb)
        entityManager.removeAll(branchDescendants.map { it.entity })
    }
}