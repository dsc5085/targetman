package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import test.dclib.geometry.VectorTestUtils

class JumpVelocitySolverTest {
    private val GRAVITY = -9.8f
    private val AGENT_SPEED = Vector2(10f, 10f)
    private val solver = JumpVelocitySolver(AGENT_SPEED, GRAVITY)

    @Test
    fun solve_SamePosition_ZeroVelocity() {
        val start = Vector2(1f, 0f)
        testSolve(start, start, 0f, 0f)
    }

    @Test
    fun solve_StraightDrop_ZeroVelocity() {
        val start = Vector2(1f, 0f)
        val end = Vector2(1f, -10f)
        testSolve(start, end, 0f, 0f)
    }

    @Test
    fun solve_StraightUp_UpwardsVelocity() {
        val start = Vector2(1f, 0f)
        val end = Vector2(1f, 2f)
        testSolve(start, end, 0f, 6.2609906f)
    }

    @Test
    fun solve_TooHighUp_Invalid() {
        val start = Vector2(1f, 0f)
        val end = Vector2(1f, 50f)
        testSolveInvalid(start, end)
    }

    @Test
    fun solve_ShortHop_SmallVelocityY() {
        val start = Vector2(1f, 0f)
        val end = Vector2(2f, 0f)
        testSolve(start, end, AGENT_SPEED.x, 0.49000007f)
    }

    @Test
    fun solve_LongHop_BigVelocityY() {
        val start = Vector2(1f, 0f)
        val end = Vector2(-18f, 0f)
        testSolve(start, end, -AGENT_SPEED.x, 9.309999f)
    }

    @Test
    fun solve_TooLongHop_Invalid() {
        val start = Vector2(1f, 0f)
        val end = Vector2(100f, 0f)
        testSolveInvalid(start, end)
    }

    @Test
    fun solve_DiagonalUp_DiagonalVelocity() {
        val start = Vector2(1f, 0f)
        val end = Vector2(-2f, 3f)
        testSolve(start, end, -3.8340578f, 7.668116f)
    }

    @Test
    fun solve_DiagonalDrop_DiagonalVelocity() {
        val start = Vector2(1f, 0f)
        val end = Vector2(3f, -10f)
        testSolve(start, end, 1.4f, 0f)
    }

    @Test
    fun solve_LongDiagonalDrop_DiagonalVelocity() {
        val start = Vector2(1f, 0f)
        val end = Vector2(-18f, -5f)
        testSolve(start, end, -AGENT_SPEED.x, 6.6784205f)
    }

    private fun testSolve(start: Vector2, end: Vector2, expectedVelocityX: Float, expectedVelocityY: Float) {
        val result = solver.solve(start, end)
        VectorTestUtils.assertEquals(expectedVelocityX, expectedVelocityY, result.velocity)
        assertTrue(result.isValid)
    }

    private fun testSolveInvalid(start: Vector2, end: Vector2) {
        val result = solver.solve(start, end)
        assertFalse(result.isValid)
    }
}