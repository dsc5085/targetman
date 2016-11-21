package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import dclib.geometry.base
import dclib.geometry.center
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.util.Maths
import net.dermetfan.gdx.physics.box2d.Box2DUtils

class JumpChecker(private val body: Body,
                  private val world: World,
                  private val jumpVelocitySolver: JumpVelocitySolver) {
    fun isValid(start: Vector2, end: Vector2): Boolean {
        val result = jumpVelocitySolver.solve(start, end)
        return result.isValid && passedSimulation(start, end, result)
    }

    private fun passedSimulation(start: Vector2, end: Vector2, result: JumpVelocityResult): Boolean {
        val simRoundingError = 0.1f
        val clonedBody = Box2DUtils.clone(body)
        clonedBody.isActive = true
        val transform = Box2dTransform(clonedBody)
        transform.setGlobal(transform.bounds.base, start)
        transform.velocity = result.velocity
        runSimulation(result.time)
        val bounds = transform.bounds
        world.destroyBody(clonedBody)
        return Maths.distance(bounds.center.x, end.x) < simRoundingError
                && Maths.distance(bounds.y, end.y) < simRoundingError
    }

    private fun runSimulation(maxTime: Float) {
        // Arbitrary value, however a smaller value is more precise
        val delta = 1f / 60f
        var currentTime = 0f
        while (currentTime < maxTime) {
            val timeStep = Math.min(delta, maxTime - currentTime)
            world.step(timeStep, Box2dUtils.VELOCITY_ITERATIONS, Box2dUtils.POSITION_ITERATIONS)
            currentTime += timeStep
        }
    }
}