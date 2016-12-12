package dc.targetman.character

import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.EntityAddedEvent
import dclib.epf.EntityManager

class AddLimbsOnEntityAdded(private val entityManager: EntityManager) : (EntityAddedEvent) -> Unit {
    override fun invoke(event: EntityAddedEvent) {
        val skeletonPart = event.entity.tryGet(SkeletonPart::class.java)
        if (skeletonPart != null) {
            entityManager.addAll(skeletonPart.getAll())
        }
    }
}