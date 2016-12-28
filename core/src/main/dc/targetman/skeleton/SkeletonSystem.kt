package dc.targetman.skeleton

import com.badlogic.gdx.math.Matrix3
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
import dclib.geometry.VectorUtils
import dclib.geometry.abs
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
        val transform = limb.entity[TransformPart::class].transform
        val newWorld = Vector2(bone.worldX, bone.worldY)
        transform.rotation = bone.worldRotationX
        val attachment = limb.getAttachments().filterIsInstance<RegionAttachment>().firstOrNull()
        updateTransform(transform, attachment, bone, newWorld)
    }

    private fun updateTransform(transform: Transform, attachment: Attachment?, bone: Bone, newWorld: Vector2) {
        if (attachment is RegionAttachment) {
            val boneScale = BoneUtils.getScale(bone)
            transform.rotation += getScaledRotation(attachment.rotation, boneScale)
            val offsetRotation = getScaledRotation(bone.worldRotationX, boneScale)
            transform.scale = calculateTransformScale(boneScale, attachment.rotation)
            val localOffset = Vector2(attachment.x, attachment.y).rotate(offsetRotation).scl(transform.scale)
            newWorld.add(localOffset)
        }
        val origin = transform.size.scl(0.5f)
        transform.setWorld(origin, newWorld)
    }

    private fun calculateTransformScale(boneScale: Vector2, attachmentRotation: Float): Vector2 {
        val boneFlipScale = VectorUtils.sign(boneScale)
        val matrix = Matrix3().rotate(attachmentRotation).scale(boneScale).rotate(-attachmentRotation)
        return matrix.getScale(Vector2()).abs().scl(boneFlipScale)
    }

    private fun getScaledRotation(degrees: Float, scale: Vector2): Float {
        return VectorUtils.toVector2(degrees, 1f).scl(scale).angle()
    }
}