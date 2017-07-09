package dc.targetman.skeleton

import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.eventing.EventDelegate

class LimbBranchDestroyedChecker(private val entityManager: EntityManager) {
    val destroyed = EventDelegate<LimbBranchDestroyedEvent>()

    init {
        entityManager.entityDestroyed.on { handleEntityDestroyed(it.entity) }
    }

    private fun handleEntityDestroyed(entity: Entity) {
        val limb = LimbUtils.find(entityManager.getAll(), entity)
        if (limb != null) {
            // TODO: Remove the need for skeletonroot entities to need a SkeletonPart
            val container = LimbUtils.findContainer(entityManager.getAll(), entity)
            if (limb.bone === limb.skeleton.rootBone && container != null) {
                entityManager.destroy(container)
            }
            destroyBranch(limb)
            val parentLimb = LimbUtils.findParent(entityManager.getAll(), limb)
            if (parentLimb != null) {
                parentLimb.detach(limb)
            }
        }
    }

    private fun destroyBranch(limb: Limb) {
        val branchDescendants = limb.getDescendants(includeLinked = true).minus(limb)
        entityManager.destroyAll(branchDescendants.map { it.entity })
        destroyed.notify(LimbBranchDestroyedEvent(limb))
    }
}