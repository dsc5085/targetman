package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import dc.targetman.mechanics.Direction

interface Agent {
    val body: Body
    val bounds: Rectangle
    val facingDirection: Direction
    val velocity: Vector2
    val speed: Vector2
    val profile: AiProfile
    val path: Path
    val steerState: SteerState
    val targetBounds: Rectangle

    fun checkCalculatePath(): Boolean
    fun moveHorizontal(direction: Direction)
    fun moveUp()
    fun moveDown()
}