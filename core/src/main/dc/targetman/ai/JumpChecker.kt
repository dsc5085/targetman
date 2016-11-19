package dc.targetman.ai

import com.badlogic.gdx.math.Vector2
import dc.targetman.physics.JumpVelocitySolver

class JumpChecker(/*private val map: Map,*/
        private val jumpVelocitySolver: JumpVelocitySolver,
        private val agentSize: Vector2) {
    fun check(start: Vector2, end: Vector2): Boolean {
        val startVelocity = jumpVelocitySolver.solve(start, end)
        return false
    }
}