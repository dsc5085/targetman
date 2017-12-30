package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import dclib.geometry.VectorUtils

object JumpVelocitySolver {
    fun solve(from: Vector2, to: Vector2, agentSpeed: Vector2, gravity: Float): JumpVelocityResult {
        val velocity = Vector2()
        val offset = VectorUtils.offset(from, to)
        val horizontalTime = Math.abs(offset.x / agentSpeed.x)
        val verticalTime: Float
        if (offset.y > 0) {
            velocity.y = getVelocityToReachHeight(offset.y, gravity)
            verticalTime = getTimeToReachVelocityY(velocity.y, 0f, gravity)
        } else {
            verticalTime = getFreefallTime(offset.y, gravity)
        }
        val airTime: Float
        if (horizontalTime > verticalTime) {
            airTime = horizontalTime
            velocity.x = agentSpeed.x * Math.signum(offset.x)
            velocity.y = getStartVelocityY(offset.y, horizontalTime, gravity)
        } else {
            airTime = verticalTime
            velocity.x = if (verticalTime == 0f) 0f else offset.x / verticalTime
        }
        return JumpVelocityResult(velocity, airTime, agentSpeed)
    }

    private fun getFreefallTime(offsetY: Float, gravity: Float): Float {
        if (offsetY > 0) {
            throw IllegalArgumentException("Offset y must be less than or equal to 0")
        }
        return Math.sqrt((2.0 * offsetY) / gravity).toFloat()
    }

    private fun getVelocityToReachHeight(height: Float, gravity: Float): Float {
        return Math.sqrt(2.0 * -gravity * height).toFloat()
    }

    private fun getTimeToReachVelocityY(startVelocityY: Float, endVelocityY: Float, gravity: Float): Float {
        return (endVelocityY - startVelocityY) / gravity
    }

    private fun getStartVelocityY(offsetY: Float, time: Float, gravity: Float): Float {
        val timeSquared = time * time
        return Math.max((-0.5f * -gravity * timeSquared - offsetY) / -time, 0f)
    }
}