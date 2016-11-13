package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.epf.parts.AiPart
import dclib.epf.Entity
import dclib.geometry.center

class Navigator(private val graphHelper: GraphHelper, private val steering: Steering, private val world: World) {
    fun navigate(entity: Entity, targetBounds: Rectangle) {
        val agent = Agent(entity, targetBounds, graphHelper)
        steering.seek(agent)
        removeReachedNodes(agent)
        updatePath(agent)
    }

    private fun removeReachedNodes(agent: Agent) {
        if (agent.belowSegment != null && graphHelper.isBelow(agent.nextNode, agent.bounds, agent.belowSegment)) {
            val newPath = if (agent.nextNode == null) agent.path else agent.path - agent.nextNode
            agent.entity.get(AiPart::class.java).path = newPath
        }
    }

    private fun updatePath(agent: Agent) {
        val targetSegment = graphHelper.getNearestBelowSegment(agent.targetBounds)
        val aiPart = agent.entity.get(AiPart::class.java)
        val updatePath = aiPart.checkUpdatePath()
        // TODO: below segment is untrustworthy since it could be a long way down
        if (updatePath && agent.belowSegment != null && targetSegment != null) {
            val agentCenter = agent.bounds.center
            val targetCenter = agent.targetBounds.center
            if (!AiUtils.isInSight(agentCenter, targetCenter, agent.profile.maxTargetDistance, world)) {
                val endNode = graphHelper.getNearestNode(targetCenter.x, targetSegment)
                val newPath = graphHelper.createPath(agentCenter.x, agent.belowSegment, endNode)
                aiPart.path = newPath
            }
        }
    }
}