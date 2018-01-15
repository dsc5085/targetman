package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import test.dclib.geometry.VectorTestUtils

class JumpVelocitySolverTest {
    private val GRAVITY = -9.8f
    private val AGENT_SPEED = Vector2(10f, 10f)

    @Test
    fun solve_SamePosition_ZeroVelocity() {
        val from = Vector2(1f, 0f)
        testSolve(from, from, 0f, 0f)
    }

    @Test
    fun solve_StraightDrop_ZeroVelocity() {
        val from = Vector2(1f, 0f)
        val to = Vector2(1f, -10f)
        testSolve(from, to, 0f, 0f)
    }

    @Test
    fun solve_StraightUp_UpwardsVelocity() {
        val from = Vector2(1f, 0f)
        val to = Vector2(1f, 2f)
        testSolve(from, to, 0f, 6.2609906f)
    }

    @Test
    fun solve_TooHighUp_Invalid() {
        val from = Vector2(1f, 0f)
        val to = Vector2(1f, 50f)
        testSolveInvalid(from, to)
    }

    @Test
    fun solve_ShortHop_SmallVelocityY() {
        val from = Vector2(1f, 0f)
        val to = Vector2(2f, 0f)
        testSolve(from, to, AGENT_SPEED.x, 0.49000007f)
    }

    @Test
    fun solve_LongHop_BigVelocityY() {
        val from = Vector2(1f, 0f)
        val to = Vector2(-18f, 0f)
        testSolve(from, to, -AGENT_SPEED.x, 9.309999f)
    }

    @Test
    fun solve_TooLongHop_Invalid() {
        val from = Vector2(1f, 0f)
        val to = Vector2(100f, 0f)
        testSolveInvalid(from, to)
    }

    @Test
    fun solve_DiagonalUp_DiagonalVelocity() {
        val from = Vector2(1f, 0f)
        val to = Vector2(-2f, 3f)
        testSolve(from, to, -3.8340578f, 7.668116f)
    }

    @Test
    fun solve_DiagonalDrop_DiagonalVelocity() {
        val from = Vector2(1f, 0f)
        val to = Vector2(3f, -10f)
        testSolve(from, to, 1.4f, 0f)
    }

    @Test
    fun solve_LongDiagonalDrop_DiagonalVelocity() {
        val from = Vector2(1f, 0f)
        val to = Vector2(-18f, -5f)
        testSolve(from, to, -AGENT_SPEED.x, 6.6784205f)
    }

    private fun testSolve(from: Vector2, to: Vector2, expectedVelocityX: Float, expectedVelocityY: Float) {
        val result = JumpVelocitySolver.solve(from, to, AGENT_SPEED, GRAVITY)
        VectorTestUtils.assertEquals(expectedVelocityX, expectedVelocityY, result.velocity)
        assertTrue(result.isValid)
    }

    private fun testSolveInvalid(from: Vector2, to: Vector2) {
        val result = JumpVelocitySolver.solve(from, to, AGENT_SPEED, GRAVITY)
        assertFalse(result.isValid)
    }
}