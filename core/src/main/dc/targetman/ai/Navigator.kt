package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.ai.graph.GraphQuery
import dclib.epf.Entity
import dclib.geometry.center

class Navigator(private val graphQuery: GraphQuery, private val world: World) {
    private val steering: Steering = Steering(graphQuery)

    fun navigate(entity: Entity, targetBounds: Rectangle) {
        val agent = Agent(entity, targetBounds, graphQuery)
        steering.seek(agent)
        removeReachedNodes(agent)
        updatePath(agent)
    }

    private fun removeReachedNodes(agent: Agent) {
        val nextNode = agent.nextNode
        if (nextNode != null && agent.bounds.contains(nextNode.position)) {
            agent.path -= nextNode
        }
    }

    private fun updatePath(agent: Agent) {
        val targetSegment = graphQuery.getNearestBelowSegment(agent.targetBounds)
        val updatePath = agent.checkUpdatePath()
        // TODO: below segment is untrustworthy since it could be a long way down
        if (updatePath && agent.belowSegment != null && targetSegment != null && agent.belowSegment !== targetSegment) {
            val agentCenter = agent.bounds.center
            val targetCenter = agent.targetBounds.center
            if (!AiUtils.isInSight(agentCenter, targetCenter, agent.profile.maxTargetDistance, world)) {
                val endNode = graphQuery.getNearestNode(targetCenter.x, targetSegment)
                val newPath = graphQuery.createPath(agentCenter.x, agent.belowSegment, endNode)
                agent.path = newPath
            }
        }
    }
}