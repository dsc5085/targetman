package dc.targetman.ai

import dclib.physics.collision.CollisionChecker
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat

class MoveAiTest {
    fun move_NotGrounded_DoesNotUpdatePath() {
        val collisionChecker: CollisionChecker
        val graph: Graph
        // create Graph where there is a node just below Agent
        val agent: Agent
        val moveAi = MoveAi(graph)
        val oldPath = agent.path
        moveAi.move(agent)
        assertThat(agent.path, `is`(oldPath))
        // Put Agent on the ground, ready for updatePath
        // Update MoveAi
        // Check Agent path is same as before
    }
}