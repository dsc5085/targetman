package dc.targetman.physics

import com.badlogic.gdx.math.Vector2

class JumpVelocityResult(velocity: Vector2, val airTime: Float, agentSpeed: Vector2) {
    private val _velocity = velocity
    val velocity get() = _velocity.cpy()

    private val _agentSpeed = agentSpeed
    val agentSpeed get() = _agentSpeed.cpy()

    val isValid get() = Math.abs(velocity.x) <= agentSpeed.x && velocity.y in 0f..agentSpeed.y
}