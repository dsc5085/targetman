package dc.targetman.skeleton

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.Attachment
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.eventing.EventDelegate
import dclib.geometry.base
import dclib.physics.Transform

class SkeletonSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    val animationApplied = EventDelegate<AnimationAppliedEvent>()

    override fun update(delta: Float, entity: Entity) {
        val skeletonPart = entity.tryGet(SkeletonPart::class)
        if (skeletonPart != null) {
            val skeleton = getTransformedSkeleton(delta, entity)
            for (limb in skeletonPart.getActiveLimbs()) {
                updateLimbTransform(limb, skeleton)
            }
        }
    }

    private fun getTransformedSkeleton(delta: Float, entity: Entity): Skeleton {
        val skeletonPart = entity[SkeletonPart::class]
        val skeleton = Skeleton(skeletonPart.skeleton)
        val animationState = skeletonPart.animationState
        animationState.update(delta)
        animationState.apply(skeleton)
        skeleton.updateWorldTransform()
        animationApplied.notify(AnimationAppliedEvent(entity, skeleton))
        skeleton.rootBone.scaleX *= skeletonPart.baseScale.x
        skeleton.rootBone.scaleY *= skeletonPart.baseScale.y
        skeleton.updateWorldTransform()
        updateRootPosition(skeleton, entity[TransformPart::class].transform)
        return skeleton
    }

    private fun updateRootPosition(skeleton: Skeleton, transform: Transform) {
        val rootYToMinYOffset = skeleton.rootBone.y - skeleton.bounds.y
        val newRootPosition = transform.bounds.base.add(0f, rootYToMinYOffset)
        skeleton.rootBone.x = newRootPosition.x
        skeleton.rootBone.y = newRootPosition.y
        skeleton.updateWorldTransform()
    }

    private fun updateLimbTransform(limb: Limb, transformedSkeleton: Skeleton) {
        val bone = transformedSkeleton.findBone(limb.name)
        val transform = limb.transform
        transform.rotation = bone.worldRotationX
        val attachment = limb.getRegionAttachment()
        updateTransform(transform, attachment, bone)
    }

    private fun updateTransform(transform: Transform, attachment: Attachment?, bone: Bone) {
        val world = Vector2(bone.worldX, bone.worldY)
        if (attachment is RegionAttachment) {
            // TODO: Figure out best way to get scale
            val offsetFromBone = SkeletonUtils.getOffset(bone, attachment, SkeletonUtils.getScale(bone))
            world.add(offsetFromBone)
            // TODO: Duplicate code with SkeletonUtils
            val boneScale = SkeletonUtils.getScale(bone)
            val attachmentScale = SkeletonUtils.calculateAttachmentScale(boneScale, attachment.rotation)
            //
            transform.rotation += SkeletonUtils.getScaledRotation(attachment.rotation, attachmentScale)
            transform.scale = attachmentScale
        }
        val origin = SkeletonUtils.getOrigin(transform.size)
        transform.setWorld(origin, world)
    }
}