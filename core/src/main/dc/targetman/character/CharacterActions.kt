package dc.targetman.character

import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.MovementPart
import dc.targetman.mechanics.Direction
import dclib.epf.Entity

object CharacterActions {
    fun move(entity: Entity, direction: Direction) {
        entity[MovementPart::class].direction = direction
    }

    fun jump(entity: Entity) {
        entity[MovementPart::class].tryJumping = true
    }

    fun aim(entity: Entity, direction: Int) {
        entity[FiringPart::class].aimDirection = direction
    }

    fun trigger(entity: Entity) {
        entity[FiringPart::class].triggered = true
    }

    fun pickup(entity: Entity) {
        entity[InventoryPart::class].tryPickup = true
    }
}
