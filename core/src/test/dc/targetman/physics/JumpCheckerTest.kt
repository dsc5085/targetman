package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World
import dclib.geometry.PolygonUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class JumpCheckerTest {
    private val GRAVITY = -9.8f
    private val AGENT_SPEED = Vector2(10f, 10f)
    private val jumpChecker = createJumpChecker()

    @Test
    fun isValid__True() {
        val isValid = jumpChecker.isValid(Vector2(0f, 10f), Vector2(2f, 13f), Vector2(0.5f, 0f))
        assertTrue(isValid)
    }

    private fun createJumpChecker(): JumpChecker {
        val solver = JumpVelocitySolver(AGENT_SPEED)
        val world = World(Vector2(0f, GRAVITY), true)
        val body = PhysicsUtils.createBody(world, BodyType.DynamicBody, PolygonUtils.createRectangleVertices(1f, 2f), false)
        body.isFixedRotation = true
        body.isBullet = true
        body.isActive = false
        return JumpChecker(body, world, solver)
    }
}