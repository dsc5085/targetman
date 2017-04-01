package dc.targetman.character

import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.StaggerPart
import dc.targetman.mechanics.Direction
import dclib.epf.Entity

object CharacterActions {
    fun move(entity: Entity, direction: Direction) {
        tryExecute(entity, { entity[MovementPart::class].direction = direction })
    }

    fun jump(entity: Entity) {
        tryExecute(entity, { entity[MovementPart::class].tryJumping = true })
    }

    fun aim(entity: Entity, direction: Int) {
        tryExecute(entity, { entity[FiringPart::class].aimDirection = direction })
    }

    fun trigger(entity: Entity) {
        tryExecute(entity, { entity[FiringPart::class].triggered = true })
    }

    fun pickup(entity: Entity) {
        tryExecute(entity, { entity[InventoryPart::class].tryPickup = true })
    }

    private fun tryExecute(entity: Entity, action: (Entity) -> Unit) {
        if (!entity[StaggerPart::class].isStaggered) {
            action.invoke(entity)
        }
    }
}
