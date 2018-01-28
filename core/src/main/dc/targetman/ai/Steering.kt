package dc.targetman.ai

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import dc.targetman.ai.graph.ConnectionType
import dc.targetman.ai.graph.DefaultConnection
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.ai.graph.Segment
import dc.targetman.mechanics.Direction
import dc.targetman.physics.JumpVelocitySolver
import dclib.geometry.base
import dclib.geometry.center
import dclib.geometry.containsX
import dclib.geometry.top
import dclib.util.FloatRange
import dclib.util.Maths

/**
 * Figures out the move actions to take to get to the next node.
 *
 * TODO: Provide an abstraction layer between fine-tuned movements (such as climbing for a frame) and path finding, e.g. climb or jump instead of move up
 */
class Steering(private val graphQuery: GraphQuery, private val gravity: Float) {
    fun update(agent: Agent) {
        if (!agent.path.isEmpty) {
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
        val connection = agent.path.currentConnection
        val toNode = connection.toNode
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
        val steerState = agent.aiPart.steerState
        when {
            steerState.climbState == ClimbState.MOVE_TO_LADDER -> {
                move(agent)
                if (agent.bounds.containsX(connection.fromNode.x)) {
                    steerState.climbState = ClimbState.CLIMBING
                }
            }
            steerState.climbState == ClimbState.DISMOUNTED -> {
                move(agent)
            }
            dismountRangeY.contains(offsetY) -> {
                agent.moveHorizontal(moveDirection)
                steerState.climbState = ClimbState.DISMOUNTED
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
        val isVerticalNodeConnection = fromNode.x == toNode.x
        if (agent.aiPart.alertTimer.isElapsed && targetSegment !== null && targetSegment === belowSegment) {
            nextX = getTargetX(agent, targetSegment)
        } else if (isVerticalNodeConnection) {
            nextX = getNextXToGetAroundEdge(agent.bounds, agent.path.currentConnection)
        } else {
            nextX = toNode.x
        }
        return nextX
    }

    private fun getNextXToGetAroundEdge(bounds: Rectangle, connection: DefaultConnection): Float {
        var nextX: Float
        if (connection.toNode.y > connection.fromNode.y) {
            val edgeNode = connection.toNode
            nextX = edgeNode.x
            val toSegment = graphQuery.getSegment(edgeNode)
            val isLeftEdgeConnection = edgeNode == toSegment.leftNode
            if (edgeNode.y > bounds.top) {
                if (isLeftEdgeConnection) {
                    nextX -= bounds.width
                } else {
                    nextX += bounds.width
                }
            }
        } else {
            val edgeNode = connection.fromNode
            nextX = edgeNode.x
            val fromSegment = graphQuery.getSegment(edgeNode)
            val isLeftEdgeConnection = edgeNode == fromSegment.leftNode
            if (edgeNode.y < bounds.top) {
                if (isLeftEdgeConnection) {
                    nextX -= bounds.width
                } else {
                    nextX += bounds.width
                }
            }
        }
        return nextX
    }

    private fun getTargetX(agent: Agent, segment: Segment): Float? {
        var nextX: Float? = null
        val agentX = agent.bounds.center.x
        val targetX = agent.targetBounds.center.x
        val distance = Maths.distance(agentX, targetX)
        val profile = agent.aiPart.profile
        if (!profile.targetDistanceRange.contains(distance)) {
            nextX = if (agentX > targetX) targetX - profile.targetDistanceRange.min
                else targetX + profile.targetDistanceRange.min
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
