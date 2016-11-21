package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World
import dclib.geometry.PolygonUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class JumpCheckerTest {
    private val gravity = -9.8f
    private val agentSpeed = Vector2(10f, 10f)
    private val solver = JumpVelocitySolver(agentSpeed, gravity)

    @Test
    fun isValid__True() {
        val world = World(Vector2(0f, gravity), true)
        val body = PhysicsUtils.createBody(world, BodyType.DynamicBody, PolygonUtils.createRectangleVertices(2f, 2f), false)
        body.isFixedRotation = true
        body.isBullet = true
        body.isActive = false
        val jumpChecker = JumpChecker(body, world, solver)
        val isValid = jumpChecker.isValid(Vector2(0f, 10f), Vector2(2f, 13f))
        assertTrue(isValid)
    }
}