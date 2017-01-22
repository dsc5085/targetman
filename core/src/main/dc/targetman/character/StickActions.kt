package dc.targetman.character

import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.WeaponPart
import dc.targetman.mechanics.Direction
import dclib.epf.Entity

object StickActions {
    fun move(entity: Entity, direction: Direction) {
        entity[MovementPart::class].direction = direction
    }

    fun jump(entity: Entity) {
        entity[MovementPart::class].tryJumping = true
    }

    fun aim(entity: Entity, direction: Int) {
        entity[WeaponPart::class].aimDirection = direction
    }

    fun trigger(entity: Entity) {
        entity[WeaponPart::class].setTriggered(true)
    }
}
