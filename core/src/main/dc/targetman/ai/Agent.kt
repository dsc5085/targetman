package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.ai.graph.DefaultNode
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.epf.parts.AiPart
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.StickActions
import dclib.epf.Entity
import dclib.epf.parts.LimbsPart
import dclib.epf.parts.TransformPart

class Agent(private val entity: Entity, val targetBounds: Rectangle, graphQuery: GraphQuery) {
    private val aiPart = entity.get(AiPart::class.java)

    val belowSegment = graphQuery.getNearestBelowSegment(bounds)

    val bounds: Rectangle
        get() = entity.get(TransformPart::class.java).transform.bounds

    val facingDirection: Direction
        get() = if (entity[LimbsPart::class.java].flipX) Direction.LEFT else Direction.RIGHT

    val velocity: Vector2
        get() = entity[TransformPart::class.java].transform.velocity

    var path: List<DefaultNode>
        get() = aiPart.path
        set(value) {
            aiPart.path = value
        }

    val nextNode: DefaultNode?
        get() = if (aiPart.path.isEmpty()) null else aiPart.path[0]

    val profile: AiProfile
        get() = aiPart.profile

    fun checkUpdatePath(): Boolean {
        return aiPart.checkUpdatePath()
    }

    fun move(direction: Direction) {
        StickActions.move(entity, direction)
    }

    fun jump() {
        StickActions.jump(entity)
    }
}
