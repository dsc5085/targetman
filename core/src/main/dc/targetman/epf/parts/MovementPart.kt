package dc.targetman.epf.parts

import com.badlogic.gdx.math.Vector2
import dclib.util.Timer

class MovementPart(
        /**
         * @return the maximum horizontal (running) and vertical (jumping) speed
         */
        val speed: Vector2,
        jumpIncreaseTime: Float,
        val limbNames: List<String>
) {
    val jumpIncreaseTimer = Timer(jumpIncreaseTime)
    var climbing = false

}