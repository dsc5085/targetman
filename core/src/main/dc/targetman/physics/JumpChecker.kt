package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import dclib.geometry.PolygonUtils
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class JumpChecker(private val world: World, private val jumpVelocitySolver: JumpVelocitySolver) {
    fun isValid(start: Vector2, end: Vector2, size: Vector2, local: Vector2): Boolean {
        val result = jumpVelocitySolver.solve(start, end)
        return result.isValid && passedSimulation(start, end, size, local, result)
    }

    private fun passedSimulation(
            start: Vector2,
            end: Vector2,
            size: Vector2,
            local: Vector2,
            result: JumpVelocityResult): Boolean {
        val body = createBody(size)
        val transform = Box2dTransform(body)
        transform.setWorld(local, start)
        transform.velocity = result.velocity
        runSimulation(result.airTime)
        val actualEnd = transform.toWorld(local)
        world.destroyBody(body)
        return areBox2dPositionsApproximatelyEqual(end, actualEnd, start)
    }

    private fun createBody(size: Vector2): Body {
        val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
        val body = PhysicsUtils.createDynamicBody(world, vertices, false)
        body.isFixedRotation = true
        body.isBullet = true
        return body
    }

    /**
     * Box2D calculations are more inaccurate the longer a body travels, thus allow an amount of error in the actual end
     * position based off of distance traveled.
     */
    private fun areBox2dPositionsApproximatelyEqual(expectedEnd: Vector2, end: Vector2, start: Vector2): Boolean {
        val minTolerance = 0.1f
        val toleranceRatioBasedOffDistance = 0.01f
        val endDifference = end.dst(expectedEnd)
        val tolerance = Math.max(start.dst(expectedEnd) * toleranceRatioBasedOffDistance, minTolerance)
        return endDifference < tolerance
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