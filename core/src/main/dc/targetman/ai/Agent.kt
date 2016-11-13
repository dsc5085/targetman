package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.epf.parts.AiPart
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.StickActions
import dclib.epf.Entity
import dclib.epf.parts.TransformPart

class Agent(private val entity: Entity, val targetBounds: Rectangle, graphHelper: GraphHelper) {
    val transform = entity.get(TransformPart::class.java).transform
    val bounds = transform.bounds
    val belowSegment = graphHelper.getNearestBelowSegment(bounds)
    private val aiPart = entity.get(AiPart::class.java)

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
