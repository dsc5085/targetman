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

class SkeletonSyncSystem(val entityManager: EntityManager) : EntitySystem(entityManager) {
    val animationApplied = EventDelegate<AnimationAppliedEvent>()

    override fun update(delta: Float, entity: Entity) {
        val skeletonPart = entity.tryGet(SkeletonPart::class)
        if (skeletonPart != null) {
            updateSkeleton(delta, entity)
            removeInactiveSlots(skeletonPart)
            updateSize(entity)
            updateRootPosition(entity)
            for (limb in skeletonPart.getLimbs()) {
                updateTransform(limb, skeletonPart.rootScale)
                updateSkeletonLinks(limb)
            }
        }
    }

    private fun removeInactiveSlots(skeletonPart: SkeletonPart) {
        val inactiveLimbs = skeletonPart.getLimbs(true).filter { !it.isActive }
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
        // TODO: Need to be able to handle scale timelines.  Currently the scale gets reset to the root scale
        skeleton.rootBone.scaleX = skeletonPart.rootScale.x
        skeleton.rootBone.scaleY = skeletonPart.rootScale.y
        skeleton.updateWorldTransform()
    }

    private fun updateSize(entity: Entity) {
        val skeleton = entity[SkeletonPart::class].skeleton
        val transform = entity[TransformPart::class].transform
        val size = Vector2(transform.size.x, skeleton.bounds.height)
        transform.setSize(size)
    }

    private fun updateRootPosition(entity: Entity) {
        val skeleton = entity[SkeletonPart::class].skeleton
        val transform = entity[TransformPart::class].transform
        val newRootPosition: Vector2
        // TODO: Make this generic, or move this to a more specific system
        if (entity[SkeletonPart::class].getLimbs().any { it.name == "muzzle" }) {
            newRootPosition = transform.center
        } else {
            val rootYToMinYOffset = skeleton.rootBone.y - skeleton.bounds.y
            newRootPosition = transform.bounds.base.add(0f, rootYToMinYOffset)
        }
        skeleton.rootBone.x = newRootPosition.x
        skeleton.rootBone.y = newRootPosition.y
        skeleton.updateWorldTransform()
    }

    private fun updateTransform(limb: Limb, rootScale: Vector2) {
        val bone = limb.bone
        val attachment = limb.getRegionAttachment()
        val transform = limb.transform
        val world = Vector2(bone.worldX, bone.worldY)
        transform.rotation = limb.bone.worldRotationX
        if (attachment != null) {
            val offsetFromBone = SkeletonUtils.getOffset(bone, attachment, limb.scale)
            world.add(offsetFromBone)
            val boneScale = limb.scale.scl(VectorUtils.inv(rootScale.abs()))
            val attachmentScale = SkeletonUtils.calculateAttachmentScale(boneScale, attachment.rotation)
            transform.setScale(attachmentScale)
            transform.rotation += VectorUtils.getScaledRotation(attachment.rotation, attachmentScale)
        }
        val origin = SkeletonUtils.getOrigin(transform.localSize)
        transform.setLocalToWorld(origin, world)
    }

    private fun updateSkeletonLinks(limb: Limb) {
        for (skeletonLink in limb.getSkeletonLinks()) {
            val childRoot = skeletonLink.root
            val newScale = childRoot.transform.scale.abs().scl(limb.flipScale)
            childRoot.transform.setScale(newScale)
            val childTransform = skeletonLink.transform
            childTransform.rotation = limb.transform.rotation
            SkeletonUtils.setWorldRotationX(childRoot.bone, limb.bone.worldRotationX)
            childTransform.setWorld(childTransform.center, limb.transform.center)
        }
    }
}