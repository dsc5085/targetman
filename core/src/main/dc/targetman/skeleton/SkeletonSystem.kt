package dc.targetman.skeleton

import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.geometry.VectorUtils
import dclib.geometry.base
import dclib.physics.Transform

class SkeletonSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val skeletonPart = entity.tryGet(SkeletonPart::class)
        if (skeletonPart != null) {
            val transform = entity[TransformPart::class].transform
            val skeleton = getTransformedSkeleton(delta, skeletonPart, transform)
            for (limb in skeletonPart.getActiveLimbs()) {
                // TODO: name should be included with limb
                val name = skeletonPart.getName(limb)
                updateLimbTransform(name, limb, skeleton)
            }
        }
    }

    private fun getTransformedSkeleton(delta: Float, skeletonPart: SkeletonPart, transform: Transform): Skeleton {
        val skeleton = Skeleton(skeletonPart.skeleton)
        val animationState = skeletonPart.animationState
        animationState.update(delta)
        animationState.apply(skeleton)
        skeleton.rootBone.scaleX *= skeletonPart.baseScale.x
        skeleton.rootBone.scaleY *= skeletonPart.baseScale.y
        skeleton.updateWorldTransform()
        updateRootPosition(skeleton, transform)
        return skeleton
    }

    private fun updateRootPosition(skeleton: Skeleton, transform: Transform) {
        val rootYToMinYOffset = skeleton.rootBone.y - skeleton.bounds.y
        val newRootPosition = transform.bounds.base.add(0f, rootYToMinYOffset)
        skeleton.rootBone.x = newRootPosition.x
        skeleton.rootBone.y = newRootPosition.y
        skeleton.updateWorldTransform()
    }

    private fun updateLimbTransform(limbName: String, limb: Entity, skeleton: Skeleton) {
        // TODO: cleanup
        val bone = skeleton.bones.single { it.data.name == limbName }
        val transform = limb[TransformPart::class].transform
        val origin = transform.size.scl(0.5f)
        val newWorld = Vector2(bone.worldX, bone.worldY)
        transform.rotation = bone.worldRotationX
        val attachment = skeleton.slots.filter { it.bone.data.name == limbName }.map { it.attachment }
                .filterIsInstance<RegionAttachment>().firstOrNull()
        val flipScale = VectorUtils.sign(Vector2(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY))
        val boneScale = Vector2(bone.worldScaleX, bone.worldScaleY).scl(flipScale)
        if (attachment is RegionAttachment) {
            transform.rotation += getScaledRotation(attachment.rotation, boneScale)
            val offsetRotation = getScaledRotation(bone.worldRotationX, boneScale)
            transform.scale = calculateTransformScale(boneScale, flipScale, attachment.rotation)
            val localOffset = Vector2(attachment.x, attachment.y).rotate(offsetRotation).scl(transform.scale)
            newWorld.add(localOffset)
        }
        transform.setWorld(origin, newWorld)
    }

    private fun calculateTransformScale(boneScale: Vector2, flipScale: Vector2, attachmentRotation: Float): Vector2 {
        val matrix = Matrix3().rotate(attachmentRotation).scale(boneScale).rotate(-attachmentRotation)
        val scale = matrix.getScale(Vector2())
        return VectorUtils.abs(scale).scl(flipScale)
    }

    private fun getScaledRotation(degrees: Float, scale: Vector2): Float {
        return VectorUtils.toVector2(degrees, 1f).scl(scale).angle()
    }
}