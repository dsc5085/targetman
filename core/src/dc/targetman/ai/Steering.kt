package dc.targetman.ai

import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.StickActions
import dclib.geometry.RectangleUtils

internal class Steering(private val graphHelper: GraphHelper) {
    fun seek(agent: Agent) {
        val moveDirection = getMoveDirection(agent)
        StickActions.move(agent.entity, moveDirection)
        jump(agent, moveDirection)
    }

    private fun getMoveDirection(agent: Agent): Direction {
        val nextX = getNextX(agent)
        var moveDirection = Direction.NONE
        if (nextX != null) {
            if (!RectangleUtils.containsX(agent.bounds, nextX)) {
                val offsetX = nextX - agent.position.x
                moveDirection = if (offsetX > 0) Direction.RIGHT else Direction.LEFT
            }
        }
        return moveDirection
    }

    private fun getNextX(agent: Agent): Float? {
        // TODO: Do not chase target if already in profile target distance range
        val targetSegment = graphHelper.getBelowSegment(agent.targetBounds)
        val onTargetSegment = targetSegment != null && targetSegment === agent.belowSegment
        return if (onTargetSegment) RectangleUtils.base(agent.targetBounds).x else agent.nextNode?.x()
    }

    private fun jump(agent: Agent, moveDirection: Direction) {
        if (agent.belowSegment != null) {
            val checkBounds = RectangleUtils.grow(agent.bounds, agent.bounds.width / 2, 0f)
            val atLeftEdge = RectangleUtils.containsX(checkBounds, agent.belowSegment.left())
            val atRightEdge = RectangleUtils.containsX(checkBounds, agent.belowSegment.right())
            val approachingEdge = atLeftEdge && moveDirection == Direction.LEFT
                    || atRightEdge && moveDirection == Direction.RIGHT
            val nextSegment = graphHelper.getSegment(agent.nextNode)
            val notOnNextSegment = nextSegment != null && agent.belowSegment !== nextSegment
            if (approachingEdge || (notOnNextSegment
                    && (agent.nextNode == null || checkBounds.y < agent.nextNode.y()))) {
                StickActions.jump(agent.entity)
            }
        }
    }
}
