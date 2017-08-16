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
        checkReachedNode(agent)
    }

    private fun calculatePath(agent: Agent) {
        val targetSegment = graphQuery.getNearestBelowSegment(agent.targetBounds)
        val belowSegment = graphQuery.getNearestBelowSegment(agent.bounds)
        if (belowSegment != null && targetSegment != null && EntityUtils.isGrounded(collisionChecker, agent.entity)
                && agent.checkCalculatePath()) {
            // TODO: Better way to approximate start and end nodes
            val agentCenter = agent.bounds.center
            val targetCenter = agent.targetBounds.center
            // TODO: Pass collisionChecker instead of world
//            if (!AiUtils.isInSight(agentCenter, targetCenter, agent.profile.maxTargetDistance, world)) {
                val endNode = graphQuery.getNearestNode(targetCenter.x, targetSegment)
                val newPath = graphQuery.createPath(agentCenter.x, belowSegment, endNode)
                agent.path = newPath
//            }
        }
    }

    private fun checkReachedNode(agent: Agent) {
        val nextNode = agent.nextNode
        if (nextNode != null && atNode(nextNode, agent.bounds)) {
            agent.path -= nextNode
        }
    }

    private fun atNode(node: DefaultNode, bounds: Rectangle): Boolean {
        val buffer = Box2dUtils.ROUNDING_ERROR
        val checkBounds = bounds.setHeight(0f).grow(buffer, buffer)
        return checkBounds.contains(node.position)
    }
}