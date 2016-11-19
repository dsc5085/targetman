package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import dclib.geometry.VectorUtils
import dclib.util.Maths

class JumpVelocitySolver(private val agentSpeed: Vector2, private val gravityY: Float) {
    fun solve(start: Vector2, end: Vector2): Vector2 {
        val velocity = Vector2()
        val offset = VectorUtils.offset(start, end)
        val horizontalTime = Math.abs(offset.x / agentSpeed.x)
        var verticalTime = 0f
        if (offset.y > 0) {
            velocity.y = getVelocityToReachHeight(offset.y)
            verticalTime = getTimeToReachVelocityY(velocity.y, 0f)
        }
        if (horizontalTime > verticalTime) {
            velocity.x = agentSpeed.x * Math.signum(offset.x)
            velocity.y = getStartVelocityY(offset.y, horizontalTime)
        } else {
            velocity.x = if (verticalTime == 0f) 0f else offset.x / verticalTime
        }
        return velocity
    }

    fun isValid(jumpVelocity: Vector2): Boolean {
        return Math.abs(jumpVelocity.x) <= agentSpeed.x && Maths.between(jumpVelocity.y, 0f, agentSpeed.y)
    }

    private fun getVelocityToReachHeight(height: Float): Float {
        return Math.sqrt(2.0 * -gravityY * height).toFloat()
    }

    private fun getTimeToReachVelocityY(startVelocityY: Float, endVelocityY: Float): Float {
        return (endVelocityY - startVelocityY) / gravityY
    }

    private fun getStartVelocityY(offsetY: Float, time: Float): Float {
        val timeSquared = time * time
        return Math.max((-0.5f * -gravityY * timeSquared - offsetY) / -time, 0f)
    }
}