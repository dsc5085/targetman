package dc.targetman.epf.parts

import dc.targetman.mechanics.Direction
import dclib.util.Timer

class MovementPart(val moveSpeed: Float,
                   /**
                    * @return approximate maximum jump speed after jump increase phase
                    */
                   val jumpSpeed: Float,
                   val limbNames: List<String>) {
    var direction = Direction.NONE
    var tryJumping = false
    val jumpIncreaseTimer = Timer(0.15f)
}