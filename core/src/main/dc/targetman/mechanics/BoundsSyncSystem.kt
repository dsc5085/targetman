package dc.targetman.mechanics

import dc.targetman.epf.parts.MovementPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem

class BoundsSyncSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        // TODO: Need better check than this, like check if the transform is in the BOUNDS collision category
        if (entity.has(MovementPart::class.java)) {
            // TODO: Is this class needed anymore?
//            moveLimbsToTransform(entity)
        }
    }
}