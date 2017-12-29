package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.character.CharacterActions
import dclib.epf.Entity
import dclib.epf.parts.TransformPart

class Agent(val entity: Entity, val targetBounds: Rectangle) {
    val bounds get() = entity[TransformPart::class].transform.bounds
    val facingDirection get() = if (entity[SkeletonPart::class].flipX) Direction.LEFT else Direction.RIGHT
    val velocity get() = entity[TransformPart::class].transform.velocity
    val nextNode get() = if (aiPart.path.isEmpty()) null else aiPart.path[0].toNode
    val profile get() = aiPart.profile

    var path
        get() = aiPart.path
        set(value) {
            aiPart.path = value
        }

    private val aiPart = entity.get(AiPart::class)

    fun checkCalculatePath(): Boolean {
        return aiPart.checkCalculatePath()
    }

    fun move(direction: Direction) {
        CharacterActions.moveHorizontal(entity, direction)
    }

    fun jump() {
        CharacterActions.moveUp(entity)
    }
}
