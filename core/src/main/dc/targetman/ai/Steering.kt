package dc.targetman.ai

import com.badlogic.gdx.math.MathUtils
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.ai.graph.Segment
import dc.targetman.mechanics.Direction
import dc.targetman.physics.JumpVelocitySolver
import dclib.geometry.base
import dclib.geometry.center
import dclib.geometry.containsX
import dclib.geometry.right
import dclib.util.Maths

class Steering(private val graphQuery: GraphQuery, private val jumpVelocitySolver: JumpVelocitySolver) {
    fun seek(agent: Agent) {
        val moveDirection = getMoveDirection(agent)
        agent.move(moveDirection)
        jump(agent)
    }

    private fun getMoveDirection(agent: Agent): Direction {
        val nextX = getNextX(agent)
        val moveDirection: Direction
        if (nextX != null) {
            val offsetX: Float
            val nextSegment = graphQuery.getSegment(agent.nextNode!!)
            val aboveNextNode = agent.bounds.y > nextSegment.y
            if (aboveNextNode && agent.bounds.x < nextSegment.left) {
                offsetX = nextSegment.left - agent.bounds.x
            } else if (aboveNextNode && agent.bounds.right > nextSegment.right) {
                offsetX = nextSegment.right - agent.bounds.right
            } else if (!agent.bounds.containsX(nextX)) {
                offsetX = nextX - agent.bounds.center.x
            } else {
                offsetX = 0f
            }
            moveDirection = Direction.from(offsetX)
        } else {
            moveDirection = faceTarget(agent)
        }
        return moveDirection
    }

    private fun getNextX(agent: Agent): Float? {
        var nextX: Float?
        val targetSegment = graphQuery.getNearestBelowSegment(agent.targetBounds)
        if (targetSegment != null && targetSegment === agent.belowSegment) {
            nextX = getNextXOnSameSegment(agent, targetSegment)
        } else {
            nextX = agent.nextNode?.x
        }
        return nextX
    }

    private fun getNextXOnSameSegment(agent: Agent, segment: Segment): Float? {
        var nextX: Float? = null
        val agentX = agent.bounds.center.x
        val targetX = agent.targetBounds.center.x
        val distance = Maths.distance(agentX, targetX)
        if (!Maths.between(distance, agent.profile.minTargetDistance, agent.profile.maxTargetDistance)) {
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
        if (agent.nextNode != null) {
            val nextSegment = graphQuery.getSegment(agent.nextNode!!)
            val notOnNextSegment = agent.belowSegment === null
                    || (nextSegment !== null && agent.belowSegment !== nextSegment)
            if (notOnNextSegment && needToIncreaseJump(agent)) {
                agent.jump()
            }
        }
    }

    private fun needToIncreaseJump(agent: Agent): Boolean {
        // TODO: bounds.base isn't accurate for jump solving
        var neededVelocityY = jumpVelocitySolver.solve(agent.bounds.base, agent.nextNode!!.position).velocity.y
        return agent.velocity.y < neededVelocityY
    }
}
