package dc.targetman.skeleton

import com.badlogic.gdx.math.Vector2
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
            removeInactiveSlots(skeletonPart)
            updateBounds(entity)
            updateRootPosition(entity)
            for (limb in skeletonPart.getActiveLimbs()) {
                updateTransform(limb, skeletonPart.baseScale)
            }
        }
    }

    private fun removeInactiveSlots(skeletonPart: SkeletonPart) {
        val inactiveLimbs = skeletonPart.getAllLimbs().filter { !it.isActive }
        for (limb in inactiveLimbs) {
            skeletonPart.skeleton.drawOrder.removeAll { it.attachment === limb.getRegionAttachment() }
        }
    }

    private fun updateSkeleton(delta: Float, entity: Entity) {
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
    }

    private fun updateBounds(entity: Entity) {
        val skeleton = entity[SkeletonPart::class].skeleton
        val transform = entity[TransformPart::class].transform
        val size = Vector2(transform.size.x, skeleton.bounds.height)
        transform.setSize(size)
    }

    private fun updateRootPosition(entity: Entity) {
        val skeleton = entity[SkeletonPart::class].skeleton
        val transform = entity[TransformPart::class].transform
        val rootYToMinYOffset = skeleton.rootBone.y - skeleton.bounds.y
        val newRootPosition = transform.bounds.base.add(0f, rootYToMinYOffset)
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
        transform.setLocalToWorld(origin, world)
    }
}