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
            val isRoot = limb.bone === limb.skeleton.rootBone
            val container = LimbUtils.findContainer(entityManager.getAll(), entity)
            if (isRoot && container != null) {
                entityManager.destroy(container)
            }
            if (limb.parent != null || isRoot) {
                limb.parent?.detach(limb)
                destroyBranch(limb)
            }
        }
    }

    private fun destroyBranch(limb: Limb) {
        destroyed.notify(LimbBranchDestroyedEvent(limb))
        val branchDescendants = limb.getDescendants().minus(limb)
        entityManager.destroyAll(branchDescendants.map { it.entity })
    }
}