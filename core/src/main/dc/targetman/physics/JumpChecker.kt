package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import dclib.geometry.PolygonUtils
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class JumpChecker(private val world: World, private val moveSpeed: Vector2) {
    fun isValid(from: Vector2, to: Vector2, size: Vector2, local: Vector2): Boolean {
        val result = JumpVelocitySolver.solve(from, to, moveSpeed, world.gravity.y)
        return result.isValid && passedSimulation(from, to, size, local, result)
    }

    private fun passedSimulation(
            from: Vector2,
            to: Vector2,
            size: Vector2,
            local: Vector2,
            result: JumpVelocityResult
    ): Boolean {
        // Since the simulation is a little inaccurate, make the size of the test body bigger to prevent false positives
        val simSizeScale = 1.1f
        val simSize = size.cpy().scl(simSizeScale)
        val simLocal = local.cpy().scl(simSizeScale)
        val body = createBody(simSize)
        val transform = Box2dTransform(body)
        transform.setLocalToWorld(simLocal, from)
        transform.velocity = result.velocity
        runSimulation(result.airTime)
        val actualTo = transform.toWorld(simLocal)
        world.destroyBody(body)
        return areBox2dPositionsApproximatelyEqual(to, from, actualTo)
    }

    private fun createBody(size: Vector2): Body {
        val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
        val body = Box2dUtils.createDynamicBody(world, vertices, false)
        body.isFixedRotation = true
        body.isBullet = true
        return body
    }

    /**
     * Box2D calculations are more inaccurate the longer a body travels, thus allow an amount of error in the actual end
     * position based off of distance traveled.
     */
    private fun areBox2dPositionsApproximatelyEqual(expectedTo: Vector2, from: Vector2, to: Vector2): Boolean {
        val minTolerance = 0.1f
        val toleranceRatioBasedOffDistance = 0.01f
        val endDifference = to.dst(expectedTo)
        val tolerance = Math.max(from.dst(expectedTo) * toleranceRatioBasedOffDistance, minTolerance)
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