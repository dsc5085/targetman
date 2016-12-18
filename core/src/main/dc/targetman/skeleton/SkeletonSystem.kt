package dc.targetman.skeleton

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.geometry.base
import dclib.physics.Transform

class SkeletonSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val skeletonPart = entity.tryGet(SkeletonPart::class.java)
        if (skeletonPart != null) {
            val skeleton = skeletonPart.skeleton
            val transform = entity[TransformPart::class.java].transform
            updateAnimation(delta, skeleton, skeletonPart.animationState)
            updateRootPosition(skeleton, transform)
            for (limb in skeletonPart.getActiveLimbs()) {
                // TODO: name should be included with limb
                val name = skeletonPart.getName(limb)
                updateLimbTransform(name, limb, skeleton)
            }
        }
    }

    private fun updateAnimation(delta: Float, skeleton: Skeleton, animationState: AnimationState) {
        animationState.update(delta)
        animationState.apply(skeleton)
        skeleton.updateWorldTransform()
    }

    private fun updateRootPosition(skeleton: Skeleton, transform: Transform) {
        val rootYToMinYOffset = skeleton.rootBone.y - skeleton.bounds.y
        val newRootPosition = transform.bounds.base.add(0f, rootYToMinYOffset)
        skeleton.rootBone.x = newRootPosition.x
        skeleton.rootBone.y = newRootPosition.y
        skeleton.updateWorldTransform()
    }

    private fun updateLimbTransform(limbName: String, limb: Entity, skeleton: Skeleton) {
        val bone = skeleton.bones.single { it.data.name == limbName }
        val scale = Vector2(bone.worldScaleX, bone.worldScaleY)
        val transform = limb[TransformPart::class.java].transform
        val origin = transform.size.scl(0.5f)
        val newGlobal = Vector2(bone.worldX, bone.worldY)
        transform.rotation = bone.worldRotationX
        val attachment = skeleton.slots.filter { it.bone.data.name == limbName }.map { it.attachment }
                .filterIsInstance<RegionAttachment>().firstOrNull()
        if (attachment is RegionAttachment) {
            transform.rotation += attachment.rotation
            val localOffset = Vector2(attachment.x, attachment.y).scl(scale).setAngle(bone.worldRotationX)
            newGlobal.add(localOffset)
        }
        transform.setGlobal(origin, newGlobal)
    }
}