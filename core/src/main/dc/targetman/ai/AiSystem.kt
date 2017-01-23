package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.character.StickActions
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityFinder
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.geometry.VectorUtils
import dclib.geometry.center
import dclib.physics.Transform
import dclib.util.Maths

class AiSystem(private val entityManager: EntityManager, private val navigator: Navigator)
    : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val aiPart = entity.tryGet(AiPart::class)
        if (aiPart != null) {
            aiPart.tick(delta)
            val target = EntityFinder.find(entityManager, Alliance.PLAYER)
            if (target != null) {
                val targetBounds = target[TransformPart::class].transform.bounds
                navigator.navigate(entity, targetBounds)
                aim(entity, targetBounds)
                StickActions.trigger(entity)
            }
        }
    }

    private fun aim(entity: Entity, targetBounds: Rectangle) {
        val muzzleName = entity.get(FiringPart::class).muzzleName
        val skeletonPart = entity.get(SkeletonPart::class)
        val muzzleEntity = skeletonPart[muzzleName].entity
        val muzzleTransform = muzzleEntity[TransformPart::class].transform
        val direction = getAimRotateDirection(muzzleTransform, targetBounds.center, skeletonPart.flipX)
        StickActions.aim(entity, direction)
    }

    /**
     * Returns float indicating how rotation should change.
     * @param to to
     * @param flipX flipX
     * @return 1 if angle should be increased, -1 if angle should be decreased, or 0 if angle shouldn't change
     */
    private fun getAimRotateDirection(muzzleTransform: Transform, to: Vector2, flipX: Boolean): Int {
        val minAngleOffset = 2f
        var direction = 0
        val offset = VectorUtils.offset(muzzleTransform.position, to)
        val angleOffset = Maths.degDistance(offset.angle(), muzzleTransform.rotation)
        if (angleOffset > minAngleOffset) {
            val fireDirection = VectorUtils.toVector2(muzzleTransform.rotation, 1f)
            direction = if (offset.y * fireDirection.x > offset.x * fireDirection.y) 1 else -1
            if (flipX) {
                direction *= -1
            }
        }
        return direction
    }
}
