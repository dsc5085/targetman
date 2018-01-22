package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.ai.graph.ConnectionType
import dc.targetman.ai.graph.DefaultConnection
import dc.targetman.ai.graph.DefaultNode
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.mechanics.EntityUtils
import dclib.geometry.center
import dclib.geometry.grow
import dclib.physics.Box2dUtils
import dclib.physics.collision.CollisionChecker
import dclib.util.FloatRange

class PathUpdater(private val graphQuery: GraphQuery, private val collisionChecker: CollisionChecker) {
    fun update(agent: Agent) {
        if (agent.aiPart.isAlert) {
            calculateTargetPath(agent)
        } else {
            calculatePatrolPath(agent)
        }
        if (!agent.path.isEmpty) {
            checkReachedNode(agent)
        }
    }

    private fun calculateTargetPath(agent: Agent) {
        // TODO: Also, recalculate path if AI is stuck and not moving for a specified amount of time, e.g. due to a bad state in its current steering
        val targetSegment = graphQuery.getNearestBelowSegment(agent.targetBounds)
        val belowSegment = graphQuery.getNearestBelowSegment(agent.bounds)
        if (belowSegment != null && targetSegment != null && EntityUtils.isGrounded(collisionChecker, agent.body)
                && agent.aiPart.checkCalculatePath()) {
            // TODO: Better way to approximate start and end nodes
            val agentCenter = agent.bounds.center
            val targetCenter = agent.targetBounds.center
            val toNode = graphQuery.getNearestNode(targetCenter.x, targetSegment)
            val newPath = graphQuery.createPath(agentCenter.x, belowSegment, toNode)
            agent.path.set(newPath)
        }
    }

    private fun checkReachedNode(agent: Agent) {
        val connection = agent.path.currentConnection
        if (atNode(connection.toNode, agent.bounds)) {
            agent.path.pop()
            agent.aiPart.steerState.reset()
        }
    }

    private fun atNode(node: DefaultNode, bounds: Rectangle): Boolean {
        val buffer = Box2dUtils.ROUNDING_ERROR
        val checkBounds = bounds.setHeight(0f).grow(buffer, buffer)
        return checkBounds.contains(node.position)
    }

    private fun calculatePatrolPath(agent: Agent) {
        val belowSegment = graphQuery.getNearestBelowSegment(agent.bounds)
        if (belowSegment != null) {
            if (agent.path.isEmpty) {
                wait(agent)
                val atLeftSide = agent.bounds.center.x < belowSegment.bounds.center.x
                val node = if (atLeftSide) belowSegment.rightNode else belowSegment.leftNode
                agent.path.set(listOf(DefaultConnection(belowSegment.leftNode, node, ConnectionType.NORMAL)))
            }
        }
    }

    private fun wait(agent: Agent) {
        val waitTimeRange = FloatRange(2f, 5f)
        agent.aiPart.waitTimer.maxTime = waitTimeRange.random()
        agent.aiPart.waitTimer.reset()
    }
}