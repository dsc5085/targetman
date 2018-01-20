package dc.targetman.mechanics.character

import com.badlogic.gdx.math.Vector2
import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.StaggerPart
import dc.targetman.mechanics.ActionKey
import dc.targetman.mechanics.ActionsPart
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.StaggerState
import dclib.epf.Entity

object CharacterActions {
    fun moveHorizontal(entity: Entity, direction: Direction) {
        val actionKey = when (direction) {
            Direction.RIGHT -> ActionKey.MOVE_RIGHT
            Direction.LEFT -> ActionKey.MOVE_LEFT
            else -> throw IllegalArgumentException("Invalid direction: $direction")
        }
        tryExecute(entity, actionKey)
    }

    // TODO: Consolidate all methods that use ActionKey
    fun jump(entity: Entity) {
        tryExecute(entity, ActionKey.JUMP)
    }

    fun climbUp(entity: Entity) {
        tryExecute(entity, ActionKey.CLIMB_UP)
    }

    fun climbDown(entity: Entity) {
        tryExecute(entity, ActionKey.CLIMB_DOWN)
    }

    fun aim(entity: Entity, targetCoord: Vector2) {
        tryExecute(entity, { entity[FiringPart::class].targetCoord = targetCoord })
    }

    fun trigger(entity: Entity) {
        tryExecute(entity, ActionKey.TRIGGER)
    }

    fun switchWeapon(entity: Entity) {
        tryExecute(entity, ActionKey.SWITCH_WEAPON)
    }

    fun pickup(entity: Entity) {
        tryExecute(entity, ActionKey.PICKUP)
    }

    private fun tryExecute(entity: Entity, actionKey: Enum<*>) {
        tryExecute(entity, { entity[ActionsPart::class][actionKey].doing = true })
    }

    private fun tryExecute(entity: Entity, action: (Entity) -> Unit) {
        if (entity[StaggerPart::class].state == StaggerState.OK) {
            action.invoke(entity)
        }
    }
}
