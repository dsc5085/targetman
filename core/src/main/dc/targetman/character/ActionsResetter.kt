package dc.targetman.character

import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.MovementPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem

class ActionsResetter(entityManager: EntityManager) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        if (entity.has(MovementPart::class)) {
            entity[MovementPart::class].tryJumping = false
        }
        if (entity.has(FiringPart::class)) {
            entity[FiringPart::class].triggered = false
        }
        if (entity.has(InventoryPart::class)) {
            entity[InventoryPart::class].tryPickup = false
        }
    }
}