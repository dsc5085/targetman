package dc.targetman.skeleton

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Skeleton
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.eventing.EventDelegate
import dclib.geometry.VectorUtils
import dclib.geometry.abs
import dclib.geometry.base

class SkeletonSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    val animationApplied = EventDelegate<AnimationAppliedEvent>()

    override fun update(delta: Float, entity: Entity) {
        val skeletonPart = entity.tryGet(SkeletonPart::class)
        if (skeletonPart != null) {
            updateSkeleton(delta, entity)
            for (limb in skeletonPart.getActiveLimbs()) {
                updateTransform(limb, skeletonPart.baseScale)
            }
        }
    }

    fun updateSkeleton(delta: Float, entity: Entity) {
        val skeletonPart = entity[SkeletonPart::class]
        val skeleton = skeletonPart.skeleton
        skeletonPart.animationState.update(delta)
        skeletonPart.animationState.apply(skeleton)
        skeleton.updateWorldTransform()
        animationApplied.notify(AnimationAppliedEvent(entity))
        // TODO: Need to be able to handle scale timelines.  Currently the scale gets reset to the basescale
        skeleton.rootBone.scaleX = skeletonPart.baseScale.x
        skeleton.rootBone.scaleY = skeletonPart.baseScale.y
        skeleton.updateWorldTransform()
        val transform = entity[TransformPart::class].transform
        updateRootPosition(skeleton, transform.bounds.base)
    }

    private fun updateRootPosition(skeleton: Skeleton, basePosition: Vector2) {
        val rootYToMinYOffset = skeleton.rootBone.y - skeleton.bounds.y
        val newRootPosition = basePosition.add(0f, rootYToMinYOffset)
        skeleton.rootBone.x = newRootPosition.x
        skeleton.rootBone.y = newRootPosition.y
        skeleton.updateWorldTransform()
    }

    private fun updateTransform(limb: Limb, baseScale: Vector2) {
        val bone = limb.bone
        val attachment = limb.getRegionAttachment()
        val transform = limb.transform
        val world = Vector2(bone.worldX, bone.worldY)
        transform.rotation = limb.bone.worldRotationX
        if (attachment != null) {
            // TODO: Figure out best way to get scale
            val offsetFromBone = SkeletonUtils.getOffset(bone, attachment, SkeletonUtils.getScale(bone))
            world.add(offsetFromBone)
            // TODO: Duplicate code with SkeletonUtils.  Also cleanup .scl call
            val boneScale = SkeletonUtils.getScale(bone).scl(VectorUtils.inv(baseScale.abs()))
            val attachmentScale = SkeletonUtils.calculateAttachmentScale(boneScale, attachment.rotation)
            //
            transform.rotation += SkeletonUtils.getScaledRotation(attachment.rotation, attachmentScale)
            transform.scale = attachmentScale
        }
        val origin = SkeletonUtils.getOrigin(transform.size)
        transform.setWorld(origin, world)
    }
}