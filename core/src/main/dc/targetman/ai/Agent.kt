package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.ai.graph.DefaultConnection
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.Direction
import dclib.epf.Entity
import dclib.epf.parts.TransformPart

class Agent(val entity: Entity, val targetBounds: Rectangle) {
    val bounds get() = entity[TransformPart::class].transform.bounds
    val facingDirection get() = if (entity[SkeletonPart::class].flipX) Direction.LEFT else Direction.RIGHT
    val velocity get() = entity[TransformPart::class].transform.velocity
    val toNode get() = aiPart.path.first().toNode
    val connection get() = aiPart.path.first() as DefaultConnection
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
}
