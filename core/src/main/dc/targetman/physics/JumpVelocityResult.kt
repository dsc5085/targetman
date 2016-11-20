package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import dclib.util.Maths

class JumpVelocityResult(val velocity: Vector2, val time: Float, private val agentSpeed: Vector2) {
    val isValid: Boolean
        get() = Math.abs(velocity.x) <= agentSpeed.x && Maths.between(velocity.y, 0f, agentSpeed.y)
}