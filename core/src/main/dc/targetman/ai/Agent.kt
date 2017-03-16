package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.character.CharacterActions
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.Direction
import dclib.epf.Entity
import dclib.epf.parts.TransformPart

class Agent(private val entity: Entity, val targetBounds: Rectangle, graphQuery: GraphQuery) {
    val belowSegment = graphQuery.getNearestBelowSegment(bounds)
    val bounds get() = entity[TransformPart::class].transform.bounds
    val facingDirection get() = if (entity[SkeletonPart::class].flipX) Direction.LEFT else Direction.RIGHT
    val velocity get() = entity[TransformPart::class].transform.velocity
    val nextNode get() = if (aiPart.path.isEmpty()) null else aiPart.path[0]
    val profile get() = aiPart.profile

    var path
        get() = aiPart.path
        set(value) {
            aiPart.path = value
        }

    private val aiPart = entity.get(AiPart::class)

    fun checkUpdatePath(): Boolean {
        return aiPart.checkUpdatePath()
    }

    fun move(direction: Direction) {
        CharacterActions.move(entity, direction)
    }

    fun jump() {
        CharacterActions.jump(entity)
    }
}
