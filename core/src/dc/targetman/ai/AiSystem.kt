package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.WeaponPart
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.EntityFinder
import dc.targetman.mechanics.StickActions
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.LimbsPart
import dclib.epf.parts.TransformPart
import dclib.geometry.Centrum
import dclib.geometry.RectangleUtils
import dclib.geometry.VectorUtils
import dclib.util.Maths

class AiSystem(private val entityManager: EntityManager, private val graphHelper: GraphHelper)
    : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val aiPart = entity.tryGet(AiPart::class.java)
        if (aiPart != null) {
            aiPart.tick(delta)
            val target = EntityFinder.findPlayer(entityManager)
            if (target != null) {
                val ai = Ai(entity)
                val targetBounds = target.get(TransformPart::class.java).transform.bounds
                navigate(ai, targetBounds)
                aim(entity, targetBounds)
                StickActions.trigger(entity)
            }
        }
    }

    private fun navigate(ai: Ai, targetBounds: Rectangle) {
        removeReachedNodes(ai)
        val moveDirection = getMoveDirection(ai, targetBounds)
        StickActions.move(ai.entity, moveDirection)
        jump(ai, moveDirection)
        updatePath(ai, targetBounds)
    }

    private fun removeReachedNodes(ai: Ai) {
        if (ai.belowSegment != null && graphHelper.isBelow(ai.nextNode, ai.bounds, ai.belowSegment)) {
            ai.path.remove(ai.nextNode)
            ai.entity.get(AiPart::class.java).path = ai.path
        }
    }

    private fun getMoveDirection(ai: Ai, targetBounds: Rectangle): Direction {
        val nextX = getNextX(ai, targetBounds)
        var moveDirection = Direction.NONE
        if (nextX != null) {
            if (!RectangleUtils.containsX(ai.bounds, nextX)) {
                val offsetX = nextX - ai.position.x
                moveDirection = if (offsetX > 0) Direction.RIGHT else Direction.LEFT
            }
        }
        return moveDirection
    }

    private fun getNextX(ai: Ai, targetBounds: Rectangle): Float? {
        var nextX: Float? = null
        val targetSegment = graphHelper.getBelowSegment(targetBounds)
        val onTargetSegment = targetSegment != null && targetSegment === ai.belowSegment
        return if (onTargetSegment) RectangleUtils.base(targetBounds).x else ai.nextNode?.x()
    }

    private fun jump(ai: Ai, moveDirection: Direction) {
        if (ai.belowSegment != null) {
            val checkBounds = RectangleUtils.grow(ai.bounds, ai.bounds.width / 2, 0f)
            val atLeftEdge = RectangleUtils.containsX(checkBounds, ai.belowSegment.left())
            val atRightEdge = RectangleUtils.containsX(checkBounds, ai.belowSegment.right())
            val approachingEdge = atLeftEdge && moveDirection == Direction.LEFT
                    || atRightEdge && moveDirection == Direction.RIGHT
            val nextSegment = graphHelper.getSegment(ai.nextNode)
            val notOnNextSegment = nextSegment != null && ai.belowSegment !== nextSegment
            if (approachingEdge || notOnNextSegment) {
                if (ai.nextNode == null || checkBounds.y < ai.nextNode.y()) {
                    StickActions.jump(ai.entity)
                }
            }
        }
    }

    private fun updatePath(ai: Ai, targetBounds: Rectangle) {
        val targetSegment = graphHelper.getBelowSegment(targetBounds)
        val updatePath = ai.entity.get(AiPart::class.java).checkUpdatePath()
        if (updatePath && ai.belowSegment != null && targetSegment != null) {
            val targetX = RectangleUtils.base(targetBounds).x
            val endNode = graphHelper.getNearestNode(targetX, targetSegment)
            val newPath = graphHelper.createPath(ai.position.x, ai.belowSegment, endNode)
            ai.entity.get(AiPart::class.java).path = newPath
        }
    }

    private fun aim(entity: Entity, targetBounds: Rectangle) {
        val centrum = entity.get(WeaponPart::class.java).centrum
        val flipX = entity.get(LimbsPart::class.java).flipX
        val targetCenter = targetBounds.getCenter(Vector2())
        val direction = getRotateDirection(centrum, targetCenter, flipX)
        StickActions.aim(entity, direction)
    }

    /**
     * Returns float indicating how rotation should change.
     * @param to to
     * @param flipX flipX
     * @return 1 if angle should be increased, -1 if angle should be decreased, or 0 if angle shouldn't change
     */
    private fun getRotateDirection(centrum: Centrum, to: Vector2, flipX: Boolean): Int {
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

    private inner class Ai(val entity: Entity) {
        val bounds = entity.get(TransformPart::class.java).transform.bounds
        val position = RectangleUtils.base(bounds)
        val belowSegment = graphHelper.getBelowSegment(bounds)
        val path = entity.get(AiPart::class.java).path
        val nextNode = if (path.isEmpty()) null else path[0]
    }
}
