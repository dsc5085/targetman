package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.character.CharacterActions
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dUtils

class DefaultAgent(private val entity: Entity, override val targetBounds: Rectangle) : Agent {
    override val body get() = Box2dUtils.getBody(entity)!!
    override val bounds get() = entity[TransformPart::class].transform.bounds
    override val facingDirection get() = if (entity[SkeletonPart::class].flipX) Direction.LEFT else Direction.RIGHT
    override val velocity get() = entity[TransformPart::class].transform.velocity
    override val speed get() = entity[MovementPart::class].speed
    override val profile get() = aiPart.profile
    override val path get() = aiPart.path
    override val steerState get() = aiPart.steerState

    private val aiPart = entity[AiPart::class]

    override fun checkCalculatePath(): Boolean {
        return aiPart.checkCalculatePath()
    }

    override fun moveHorizontal(direction: Direction) {
        CharacterActions.moveHorizontal(entity, direction)
    }

    override fun jump() {
        CharacterActions.jump(entity)
    }

    override fun climbUp() {
        CharacterActions.climbUp(entity)
    }

    override fun climbDown() {
        CharacterActions.climbDown(entity)
    }
}
