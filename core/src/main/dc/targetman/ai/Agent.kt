package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.ai.graph.DefaultNode
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.epf.parts.AiPart
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.StickActions
import dclib.epf.Entity
import dclib.epf.parts.LimbsPart
import dclib.epf.parts.TransformPart
import dclib.geometry.grow
import dclib.physics.Box2dUtils

class Agent(private val entity: Entity, val targetBounds: Rectangle, graphQuery: GraphQuery) {
    val transform = entity.get(TransformPart::class.java).transform
    val bounds = transform.bounds.grow(Box2dUtils.ROUNDING_ERROR, 0f)
    val belowSegment = graphQuery.getNearestBelowSegment(bounds)
    private val aiPart = entity.get(AiPart::class.java)

    var path: List<DefaultNode>
        get() = aiPart.path
        set(value) {
            aiPart.path = value
        }

    val facingDirection: Direction
        get() = if (entity[LimbsPart::class.java].flipX) Direction.LEFT else Direction.RIGHT

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
