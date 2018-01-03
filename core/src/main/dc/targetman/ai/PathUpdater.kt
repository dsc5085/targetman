package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.ai.graph.DefaultNode
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.mechanics.EntityUtils
import dclib.geometry.center
import dclib.geometry.grow
import dclib.physics.Box2dUtils
import dclib.physics.collision.CollisionChecker

class PathUpdater(private val graphQuery: GraphQuery, private val collisionChecker: CollisionChecker) {
    fun update(agent: Agent) {
        calculatePath(agent)
        if (agent.path.isNotEmpty) {
            checkReachedNode(agent)
        }
    }

    private fun calculatePath(agent: Agent) {
        val targetSegment = graphQuery.getNearestBelowSegment(agent.targetBounds)
        val belowSegment = graphQuery.getNearestBelowSegment(agent.bounds)
        if (belowSegment != null && targetSegment != null && EntityUtils.isGrounded(collisionChecker, agent.body)
                && agent.checkCalculatePath()) {
            // TODO: Better way to approximate start and end nodes
            val agentCenter = agent.bounds.center
            val targetCenter = agent.targetBounds.center
            // TODO: Pass collisionChecker instead of world
//            if (!AiUtils.isInSight(agentCenter, targetCenter, agent.profile.maxTargetDistance, world)) {
                val toNode = graphQuery.getNearestNode(targetCenter.x, targetSegment)
                val newPath = graphQuery.createPath(agentCenter.x, belowSegment, toNode)
                agent.path.set(newPath)
//            }
        }
    }

    private fun checkReachedNode(agent: Agent) {
        val connection = agent.path.currentConnection
        if (atNode(connection.toNode, agent.bounds)) {
            agent.path.pop()
            agent.steerState.reset()
        }
    }

    private fun atNode(node: DefaultNode, bounds: Rectangle): Boolean {
        val buffer = Box2dUtils.ROUNDING_ERROR
        val checkBounds = bounds.setHeight(0f).grow(buffer, buffer)
        return checkBounds.contains(node.position)
    }
}