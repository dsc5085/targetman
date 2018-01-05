package dc.targetman.ai

import com.badlogic.gdx.math.MathUtils
import dc.targetman.ai.graph.ConnectionType
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.ai.graph.Segment
import dc.targetman.mechanics.Direction
import dc.targetman.physics.JumpVelocitySolver
import dclib.geometry.base
import dclib.geometry.center
import dclib.geometry.containsX
import dclib.util.FloatRange
import dclib.util.Maths

/**
 * Figures out the move actions to take to get to the next node.
 *
 * TODO: Provide an abstraction layer between fine-tuned movements (such as climbing for a frame) and path finding, e.g. climb or jump instead of move up
 */
class Steering(private val graphQuery: GraphQuery, private val gravity: Float) {
    fun update(agent: Agent) {
        if (agent.path.isNotEmpty) {
            when (agent.path.currentConnection.type) {
                ConnectionType.NORMAL -> {
                    move(agent)
                    jump(agent)
                }
                ConnectionType.CLIMB -> {
                    climb(agent)
                }
            }
        }
    }

    private fun jump(agent: Agent) {
        val belowSegment = graphQuery.getNearestBelowSegment(agent.bounds)
        val toSegment = graphQuery.getSegment(agent.path.currentConnection.toNode)
        val notOnToSegment = belowSegment == null || belowSegment != toSegment
        if (notOnToSegment && needToIncreaseJump(agent)) {
            agent.jump()
        }
    }

    private fun needToIncreaseJump(agent: Agent): Boolean {
        val neededVelocityY = JumpVelocitySolver.solve(
                agent.bounds.base, agent.path.currentConnection.toNode.position, agent.speed, gravity).velocity.y
        // Edge case: don't attempt to jump while falling, otherwise it might trigger the climbing since its mapped to the same action as jumping
        val isFalling = agent.velocity.y < 0
        return !isFalling && agent.velocity.y < neededVelocityY
    }

    private fun climb(agent: Agent) {
        val toNode = agent.path.currentConnection.toNode
        val toSegment = graphQuery.getSegment(toNode)
        val moveDirection: Direction
        if (toNode == toSegment.leftNode) {
            moveDirection = Direction.RIGHT
        } else {
            moveDirection = Direction.LEFT
        }
        if (moveDirection != agent.facingDirection) {
            agent.moveHorizontal(moveDirection)
        }

        val dismountBufferRatio = 0.05f
        val agentY = agent.bounds.base.y
        val offsetY = toNode.y - agentY
        val dismountBufferY = agent.bounds.height * dismountBufferRatio
        val dismountRangeY = FloatRange(-dismountBufferY, dismountBufferY)
        when {
            agent.steerState.dismounted -> {
                move(agent)
            }
            dismountRangeY.contains(offsetY) || agent.steerState.dismounted -> {
                agent.moveHorizontal(moveDirection)
                agent.steerState.dismounted = true
            }
            agentY < toNode.y -> agent.climbUp()
            agentY > toNode.y -> agent.climbDown()
        }
    }

    private fun move(agent: Agent) {
        val moveDirection = getMoveDirection(agent)
        if (moveDirection != Direction.NONE) {
            agent.moveHorizontal(moveDirection)
        }
    }

    private fun getMoveDirection(agent: Agent): Direction {
        val nextX = getNextX(agent)
        val moveDirection: Direction
        if (nextX == null) {
            moveDirection = faceTarget(agent)
        } else if (agent.bounds.containsX(nextX)) {
            moveDirection = Direction.NONE
        } else {
            moveDirection = Direction.from(nextX - agent.bounds.center.x)
        }
        return moveDirection
    }

    private fun getNextX(agent: Agent): Float? {
        val nextX: Float?
        val targetSegment = graphQuery.getNearestBelowSegment(agent.targetBounds)
        val belowSegment = graphQuery.getNearestBelowSegment(agent.bounds)
        val toNode = agent.path.currentConnection.toNode
        val fromNode = agent.path.currentConnection.fromNode
        val toSegment = graphQuery.getSegment(toNode)
        val fromSegment = graphQuery.getSegment(fromNode)
        val isVerticalNodeConnection = fromNode.x == toNode.x
        val isLeftEdgeConnection = fromNode == fromSegment.leftNode || toNode == toSegment.leftNode
        val isRightEdgeConnection = fromNode == fromSegment.rightNode || toNode == toSegment.rightNode
        // TODO: Not going up stairs. If below, nextX is at outside edge. If above, nextX is at inside
        if (targetSegment !== null && targetSegment === belowSegment) {
            nextX = getNextXOnSameSegment(agent, targetSegment)
        } else if (isVerticalNodeConnection && isLeftEdgeConnection) {
            nextX = toNode.x - agent.bounds.width
        } else if (isVerticalNodeConnection && isRightEdgeConnection) {
            nextX = toNode.x + agent.bounds.width
        } else {
            nextX = toNode.x
        }
        return nextX
    }

    private fun getNextXOnSameSegment(agent: Agent, segment: Segment): Float? {
        var nextX: Float? = null
        val agentX = agent.bounds.center.x
        val targetX = agent.targetBounds.center.x
        val distance = Maths.distance(agentX, targetX)
        if (distance !in agent.profile.minTargetDistance..agent.profile.maxTargetDistance) {
            if (agentX > targetX) {
                nextX = targetX - agent.profile.minTargetDistance
            } else {
                nextX = targetX + agent.profile.minTargetDistance
            }
            nextX = MathUtils.clamp(nextX, segment.left, segment.right)
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
}
