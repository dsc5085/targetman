package dc.targetman.ai

import com.badlogic.gdx.math.MathUtils
import dc.targetman.ai.graph.ConnectionType
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.ai.graph.Segment
import dc.targetman.epf.parts.MovementPart
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.character.CharacterActions
import dc.targetman.physics.JumpVelocitySolver
import dclib.geometry.base
import dclib.geometry.center
import dclib.geometry.containsX
import dclib.util.FloatRange
import dclib.util.Maths

/**
 * Figures out the move actions to take to get to the next node.
 */
class Steering(private val graphQuery: GraphQuery, private val gravity: Float) {
    fun update(agent: Agent) {
        if (agent.path.isNotEmpty) {
            when (agent.path.currentConnection.type) {
                ConnectionType.NORMAL -> {
                    val moveDirection = getMoveDirection(agent)
                    if (moveDirection != Direction.NONE) {
                        CharacterActions.moveHorizontal(agent.entity, moveDirection)
                    }
                    jump(agent)
                }
                ConnectionType.CLIMB -> {
                    climb(agent)
                }
            }
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
        if (targetSegment !== null && targetSegment === belowSegment) {
            nextX = getNextXOnSameSegment(agent, targetSegment)
        } else {
            nextX = agent.toNode?.x
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

    private fun jump(agent: Agent) {
        if (agent.toNode != null) {
            val belowSegment = graphQuery.getNearestBelowSegment(agent.bounds)
            val nextSegment = graphQuery.getSegment(agent.toNode)
            val notOnNextSegment = belowSegment == null || belowSegment != nextSegment
            if (notOnNextSegment && needToIncreaseJump(agent)) {
                CharacterActions.moveUp(agent.entity)
            }
        }
    }

    private fun needToIncreaseJump(agent: Agent): Boolean {
        val speed = agent.entity[MovementPart::class].speed
        val neededVelocityY = JumpVelocitySolver.solve(
                agent.bounds.base, agent.toNode!!.position, speed, gravity).velocity.y
        return agent.velocity.y < neededVelocityY
    }

    private fun climb(agent: Agent) {
        val dismountBufferRatio = 0.25f
        val agentY = agent.bounds.base.y
        val offsetY = agent.toNode.y - agentY
        val dismountBufferY = agent.bounds.height * dismountBufferRatio
        val dismountRangeY = FloatRange(-dismountBufferY, dismountBufferY)
        when {
            dismountRangeY.contains(offsetY) ->
                CharacterActions.moveHorizontal(agent.entity, Direction.RIGHT)
            agentY < agent.toNode.y -> CharacterActions.moveUp(agent.entity)
            agentY > agent.toNode.y -> CharacterActions.moveDown(agent.entity)
        }
    }
}
