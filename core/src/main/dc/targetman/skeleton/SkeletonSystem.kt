package dc.targetman.skeleton

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
        val flipScale = VectorUtils.sign(Vector2(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY))
        val scale = Vector2(bone.worldScaleX, bone.worldScaleY).scl(flipScale)
        val transform = limb[TransformPart::class].transform
        val origin = transform.size.scl(0.5f)
        val newWorld = Vector2(bone.worldX, bone.worldY)
        transform.rotation = bone.worldRotationX
        val attachment = skeleton.slots.filter { it.bone.data.name == limbName }.map { it.attachment }
                .filterIsInstance<RegionAttachment>().firstOrNull()
        var attachmentScaledRotation = 0f
        if (attachment is RegionAttachment) {
            attachmentScaledRotation = getScaledRotation(attachment.rotation, scale)
            transform.rotation += attachmentScaledRotation
            val offsetRotation = getScaledRotation(bone.worldRotationX, scale)
            val localOffset = Vector2(attachment.x, attachment.y).rotate(offsetRotation).scl(scale)
            newWorld.add(localOffset)
        }
        transform.scale = VectorUtils.abs(Vector2(bone.worldScaleX, bone.worldScaleY).rotate(attachmentScaledRotation)).scl(flipScale)
        transform.setWorld(origin, newWorld)
    }

    private fun getScaledRotation(degrees: Float, scale: Vector2): Float {
        return VectorUtils.toVector2(degrees, 1f).scl(scale).angle()
    }
}