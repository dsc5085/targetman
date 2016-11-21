package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import dclib.geometry.VectorUtils

class JumpVelocitySolver(private val agentSpeed: Vector2, private val gravityY: Float) {
    fun solve(start: Vector2, end: Vector2): JumpVelocityResult {
        val velocity = Vector2()
        val offset = VectorUtils.offset(start, end)
        val horizontalTime = Math.abs(offset.x / agentSpeed.x)
        val verticalTime: Float
        if (offset.y > 0) {
            velocity.y = getVelocityToReachHeight(offset.y)
            verticalTime = getTimeToReachVelocityY(velocity.y, 0f)
        } else {
            verticalTime = getFreefallTime(offset.y)
        }
        val time: Float
        if (horizontalTime > verticalTime) {
            time = horizontalTime
            velocity.x = agentSpeed.x * Math.signum(offset.x)
            velocity.y = getStartVelocityY(offset.y, horizontalTime)
        } else {
            time = verticalTime
            velocity.x = if (verticalTime == 0f) 0f else offset.x / verticalTime
        }
        return JumpVelocityResult(velocity, time, agentSpeed)
    }

    private fun getFreefallTime(offsetY: Float): Float {
        if (offsetY > 0) {
            throw IllegalArgumentException("Offset y must be less than or equal to 0")
        }
        return Math.sqrt((2.0 * offsetY) / gravityY).toFloat()
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