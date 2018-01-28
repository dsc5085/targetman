package dc.targetman.epf.parts

import com.badlogic.gdx.math.Vector2
import dclib.util.Timer

class MovementPart(
        /**
         * @return the maximum horizontal (running) and vertical (jumping) speed
         */
        val maxSpeed: Vector2,
        jumpIncreaseTime: Float,
        val limbNames: List<String>
) {
    companion object {
        val WALK_SPEED_RATIO: Float = 0.2f
    }

    var runSpeedRatio: Float = 1f
    val jumpIncreaseTimer = Timer(jumpIncreaseTime)
    var climbing = false
}