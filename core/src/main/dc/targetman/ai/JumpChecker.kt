package dc.targetman.ai

import com.badlogic.gdx.math.Vector2
import dc.targetman.physics.JumpVelocityResult
import dc.targetman.physics.JumpVelocitySolver
import dc.targetman.physics.Simulator

class JumpChecker(private val simulator: Simulator,
                  private val jumpVelocitySolver: JumpVelocitySolver) {
    fun check(start: Vector2, end: Vector2): Boolean {
        val result = jumpVelocitySolver.solve(start, end)
        return result.isValid && !isColliding(start, result)
    }

    private fun isColliding(start: Vector2, result: JumpVelocityResult): Boolean {
        // Arbitrary value.  This is just the preferred framerate for games
        val delta = 1f / 60f
//        simulator.collided.on()
        simulator.run(result.time, delta)
        return false
    }

//    fun willCollide(start: Vector2, result: JumpVelocityResult): Boolean {
//        val numTimeIntervals = 20
//        val timeInterval = result.time / numTimeIntervals
//        var currentTime = 0f
//        var currentPosition = start
//        var currentVelocity = result.velocity
//        while (currentTime < result.time) {
//            val nextTime = currentTime + timeInterval
//            val nextPosition = currentPosition.cpy().add(currentVelocity)
//            if (willCollide(currentPosition, nextPosition)) {
//                return true
//            }
//            currentVelocity.y += gravity
//            currentPosition = nextPosition
//            currentTime = nextTime
//        }
//        return false
//    }
//
//    fun willCollide(start: Vector2, end: Vector2): Boolean {
//        val points = bresenham.line(start.x.toInt(), start.y.toInt(), start.x.toInt(), start.y.toInt())
//        for (point in points) {
//            if (willCollide(point)) {
//                return true
//            }
//        }
//        return false
//    }
//
//    private fun willCollide(point: GridPoint2): Boolean {
//    }
}