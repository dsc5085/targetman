package dc.targetman.ai

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import dc.targetman.mechanics.Direction
import dclib.geometry.center
import dclib.geometry.containsX
import dclib.util.Maths

class Steering(private val graphHelper: GraphHelper) {
    fun seek(agent: Agent) {
        val moveDirection = getMoveDirection(agent)
        agent.move(moveDirection)
        jump(agent)
    }

    private fun getMoveDirection(agent: Agent): Direction {
        val nextX = getNextX(agent)
        val moveDirection: Direction
        if (nextX != null && !agent.bounds.containsX(nextX)) {
            val offsetX = nextX - agent.bounds.center.x
            moveDirection = Direction.from(offsetX)
        } else {
            moveDirection = faceTarget(agent)
        }
        return moveDirection
    }

    private fun getNextX(agent: Agent): Float? {
        var nextX: Float?
        val targetSegment = graphHelper.getNearestBelowSegment(agent.targetBounds)
        if (targetSegment != null && targetSegment === agent.belowSegment) {
            nextX = getNextXOnSameSegment(agent, targetSegment)
        } else {
            nextX = agent.nextNode?.x()
        }
        return nextX
    }

    private fun getNextXOnSameSegment(agent: Agent, segment: Segment): Float? {
        var nextX: Float? = null
        val agentX = agent.bounds.center.x
        val targetX = agent.targetBounds.center.x
        val edgeBuffer = getEdgeBuffer(agent.bounds)
        val distance = Maths.distance(agentX, targetX)
        if (!Maths.between(distance, agent.profile.minTargetDistance, agent.profile.maxTargetDistance)) {
            if (agentX > targetX) {
                nextX = targetX - agent.profile.minTargetDistance
            } else {
                nextX = targetX + agent.profile.minTargetDistance
            }
            nextX = MathUtils.clamp(nextX, segment.left + edgeBuffer, segment.right - edgeBuffer)
        }
        return nextX
    }

    private fun faceTarget(agent: Agent): Direction {
        var moveDirection = Direction.NONE
        val offsetX = agent.targetBounds.center.x - agent.bounds.center.x
        val directionToTarget = Direction.from(offsetX)
        if (agent.facingDirection !== directionToTarget) {
            moveDirection = directionToTarget
        }
        return moveDirection
    }

    private fun jump(agent: Agent) {
        if (agent.belowSegment != null) {
            val nextSegment = graphHelper.getSegment(agent.nextNode)
            val notOnNextSegment = nextSegment != null && agent.belowSegment !== nextSegment
            if (notOnNextSegment) {
                agent.jump()
            }
        }
    }

    private fun getEdgeBuffer(bounds: Rectangle): Float {
        val edgeBufferScale = 1
        return bounds.width * edgeBufferScale
    }
}
