package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import dclib.util.Maths

class JumpVelocityResult(velocity: Vector2, val airTime: Float, agentSpeed: Vector2) {
    private val _velocity: Vector2 = velocity
    val velocity: Vector2
        get() = _velocity.cpy()

    private val _agentSpeed: Vector2 = agentSpeed
    val agentSpeed: Vector2
        get() = _agentSpeed.cpy()

    val isValid: Boolean
        get() = Math.abs(velocity.x) <= agentSpeed.x && Maths.between(velocity.y, 0f, agentSpeed.y)
}