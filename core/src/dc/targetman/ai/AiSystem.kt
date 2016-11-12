package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.WeaponPart
import dc.targetman.mechanics.EntityFinder
import dc.targetman.mechanics.StickActions
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.LimbsPart
import dclib.epf.parts.TransformPart
import dclib.geometry.Centrum
import dclib.geometry.VectorUtils
import dclib.geometry.center
import dclib.util.Maths

class AiSystem(private val entityManager: EntityManager, private val graphHelper: GraphHelper,
               private val world: World) : EntitySystem(entityManager) {
    private val steering = Steering(graphHelper)

    override fun update(delta: Float, entity: Entity) {
        val aiPart = entity.tryGet(AiPart::class.java)
        if (aiPart != null) {
            aiPart.tick(delta)
            val target = EntityFinder.findPlayer(entityManager)
            if (target != null) {
                val targetBounds = target.get(TransformPart::class.java).transform.bounds
                val agent = Agent(entity, targetBounds, graphHelper)
                navigate(agent)
                aim(entity, targetBounds)
//                StickActions.trigger(entity)
            }
        }
    }

    private fun navigate(agent: Agent) {
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

    private fun aim(entity: Entity, targetBounds: Rectangle) {
        val centrum = entity.get(WeaponPart::class.java).centrum
        val flipX = entity.get(LimbsPart::class.java).flipX
        val direction = getAimRotateDirection(centrum, targetBounds.center, flipX)
        StickActions.aim(entity, direction)
    }

    /**
     * Returns float indicating how rotation should change.
     * @param to to
     * @param flipX flipX
     * @return 1 if angle should be increased, -1 if angle should be decreased, or 0 if angle shouldn't change
     */
    private fun getAimRotateDirection(centrum: Centrum, to: Vector2, flipX: Boolean): Int {
        val minAngleOffset = 2f
        var direction = 0
        val offset = VectorUtils.offset(centrum.position, to)
        val angleOffset = Maths.degDistance(offset.angle(), centrum.rotation)
        if (angleOffset > minAngleOffset) {
            val fireDirection = VectorUtils.toVector2(centrum.rotation, 1f)
            direction = if (offset.y * fireDirection.x > offset.x * fireDirection.y) 1 else -1
            if (flipX) {
                direction *= -1
            }
        }
        return direction
    }
}
