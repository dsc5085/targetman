package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.ai.graph.DefaultNode
import dc.targetman.ai.graph.GraphQuery
import dclib.epf.Entity
import dclib.geometry.center
import dclib.geometry.grow
import dclib.physics.Box2dUtils
import dclib.util.Maths

class Navigator(private val graphQuery: GraphQuery, private val steering: Steering, private val world: World) {
    fun navigate(entity: Entity, targetBounds: Rectangle) {
        val agent = Agent(entity, targetBounds, graphQuery)
        steering.seek(agent)
        updatePath(agent)
        val nextNode = agent.nextNode
        if (nextNode != null && atNode(nextNode, agent.bounds)) {
            agent.path -= nextNode
        }
    }

    private fun updatePath(agent: Agent) {
        val targetSegment = graphQuery.getNearestBelowSegment(agent.targetBounds)
        if (agent.belowSegment != null && targetSegment != null
                && Maths.distance(agent.bounds.y, agent.belowSegment.y) < Box2dUtils.ROUNDING_ERROR
                && agent.checkUpdatePath()) {
            // TODO: Better way to approximate start and end nodes
            val agentCenter = agent.bounds.center
            val targetCenter = agent.targetBounds.center
            if (!AiUtils.isInSight(agentCenter, targetCenter, agent.profile.maxTargetDistance, world)) {
                val endNode = graphQuery.getNearestNode(targetCenter.x, targetSegment)
                val newPath = graphQuery.createPath(agentCenter.x, agent.belowSegment, endNode)
                agent.path = newPath
            }
        }
    }

    private fun atNode(node: DefaultNode, bounds: Rectangle): Boolean {
        val buffer = Box2dUtils.ROUNDING_ERROR
        val checkBounds = bounds.setHeight(0f).grow(buffer, buffer)
        return checkBounds.contains(node.position)
    }
}