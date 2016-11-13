package dc.targetman.ai

import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.StickActions
import dclib.epf.parts.LimbsPart
import dclib.epf.parts.TransformPart
import dclib.geometry.center
import dclib.geometry.containsX
import dclib.geometry.grow

internal class Steering(private val graphHelper: GraphHelper) {
    fun seek(agent: Agent) {
        val moveDirection = getMoveDirection(agent)
        StickActions.move(agent.entity, moveDirection)
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
        var nextX: Float? = null
        val targetSegment = graphHelper.getNearestBelowSegment(agent.targetBounds)
        val onTargetSegment = targetSegment != null && targetSegment === agent.belowSegment
        if (onTargetSegment) {
            val distanceToTarget = agent.bounds.center.dst(agent.targetBounds.center)
            if (!isApproachingEdge(agent) && distanceToTarget > agent.profile.maxTargetDistance) {
                nextX = agent.targetBounds.center.x
            }
        } else {
            nextX = agent.nextNode?.x()
        }
        return nextX
    }

    private fun faceTarget(agent: Agent): Direction {
        var moveDirection = Direction.NONE
        val offsetX = agent.targetBounds.center.x - agent.bounds.center.x
        val directionToTarget = Direction.from(offsetX)
        val flipX = agent.entity[LimbsPart::class.java].flipX
        val currentDirection = if (flipX) Direction.LEFT else Direction.RIGHT
        if (currentDirection !== directionToTarget) {
            moveDirection = directionToTarget
        }
        return moveDirection
    }

    private fun jump(agent: Agent) {
        if (agent.belowSegment != null) {
            val nextSegment = graphHelper.getSegment(agent.nextNode)
            val notOnNextSegment = nextSegment != null && agent.belowSegment !== nextSegment
            if (isApproachingEdge(agent) || (notOnNextSegment
                    && (agent.nextNode == null || agent.bounds.y < agent.nextNode.y()))) {
                StickActions.jump(agent.entity)
            }
        }
    }

    private fun isApproachingEdge(agent: Agent): Boolean {
        val checkBoundsScale = 1
        var isApproachingEdge = false
        if (agent.belowSegment != null) {
            val checkBounds = agent.bounds.grow(agent.bounds.width * checkBoundsScale, 0f)
            val atLeftEdge = checkBounds.containsX(agent.belowSegment.left)
            val atRightEdge = checkBounds.containsX(agent.belowSegment.right)
            val velocityX = agent.entity[TransformPart::class.java].transform.velocity.x
            isApproachingEdge = atLeftEdge && velocityX < 0 || atRightEdge && velocityX > 0
        }
        return isApproachingEdge
    }
}
