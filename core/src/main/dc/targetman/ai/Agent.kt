package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import dc.targetman.mechanics.Direction
import dclib.epf.Entity

interface Agent {
    val body: Body
    val bounds: Rectangle
    val facingDirection: Direction
    val velocity: Vector2
    val speed: Vector2
    val profile: AiProfile
    val path: Path
    val steerState: SteerState
    val eye: Vector2
    val target: Entity
    val targetBounds: Rectangle

    fun checkCalculatePath(): Boolean
    fun moveHorizontal(direction: Direction)
    fun jump()
    fun climbUp()
    fun climbDown()
}