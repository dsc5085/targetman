package dc.targetman.mechanics

import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.WeaponPart
import dclib.epf.Entity

object StickActions {
    fun move(entity: Entity, direction: Direction) {
        entity[MovementPart::class].direction = direction
    }

    fun jump(entity: Entity) {
        entity[MovementPart::class].tryJumping = true
    }

    fun aim(entity: Entity, direction: Int) {
        // TODO:
        //		entity.get(WeaponPart.class).setAimDirection(direction);
    }

    fun trigger(entity: Entity) {
        entity[WeaponPart::class].setTriggered(true)
    }
}