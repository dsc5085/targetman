package dc.targetman.skeleton

import com.badlogic.gdx.math.Rectangle
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
import dclib.geometry.size

class SkeletonSyncSystem(val entityManager: EntityManager) : EntitySystem(entityManager) {
    val animationApplied = EventDelegate<AnimationAppliedEvent>()

    override fun update(delta: Float, entity: Entity) {
        val skeletonPart = entity.tryGet(SkeletonPart::class)
        if (skeletonPart != null && skeletonPart.isEnabled) {
            updateSkeleton(delta, entity)
            // TODO: Call updateSize(entity) here. Currently this causes issue with the AI
            updateRootPosition(entity)
            updateLimbs(skeletonPart)
        }
    }

    private fun updateSkeleton(delta: Float, entity: Entity) {
        val skeletonPart = entity[SkeletonPart::class]
        val skeleton = skeletonPart.skeleton
        skeletonPart.animationState.update(delta)
        skeletonPart.animationState.apply(skeleton)
        skeleton.updateWorldTransform()
        animationApplied.notify(AnimationAppliedEvent(entity))
        // TODO: Need to be able to handle scale timelines.  Currently the scale gets reset to the root scale.
        skeleton.rootBone.scaleX = skeletonPart.root.scale.x
        skeleton.rootBone.scaleY = skeletonPart.root.scale.y
        skeleton.updateWorldTransform()
    }

    private fun updateLimbs(skeletonPart: SkeletonPart) {
        for (limb in skeletonPart.getLimbs(LinkType.STRONG)) {
            updateTransform(limb, skeletonPart.root.scale)
            updateLinks(limb)
        }
    }

    private fun updateTransform(limb: Limb, rootScale: Vector2) {
        val bone = limb.bone
        val attachment = limb.getRegionAttachment()
        val transform = limb.transform
        val world = Vector2(bone.worldX, bone.worldY)
        transform.rotation = limb.bone.worldRotationX
        if (attachment != null) {
            val offsetFromBone = SkeletonUtils.getOffset(bone, attachment, limb.spineScale)
            world.add(offsetFromBone)
            val boneScale = limb.spineScale.scl(VectorUtils.inv(rootScale.abs()))
            val attachmentScale = SkeletonUtils.calculateAttachmentScale(boneScale, attachment.rotation)
            transform.setScale(attachmentScale)
            transform.rotation += VectorUtils.getScaledRotation(attachment.rotation, attachmentScale)
        }
        val origin = transform.localCenter
        transform.setLocalToWorld(origin, world)
    }

    private fun updateLinks(limb: Limb) {
        for (link in limb.getLinks(LinkType.WEAK)) {
            val linkRootLimb = link.limb
            val newScale = link.scale.abs().scl(limb.flipScale)
            link.scale.set(newScale)
            val childTransform = link.limb.transform
            childTransform.rotation = limb.transform.rotation
            SkeletonUtils.setWorldRotationX(linkRootLimb.bone, limb.bone.worldRotationX)
            childTransform.setWorld(childTransform.center, limb.transform.center)
        }
    }

    private fun updateSize(entity: Entity) {
        val boundingLimbsPart = entity.tryGet(BoundingSlotsPart::class)
        if (boundingLimbsPart != null) {
            val skeletonBounds = getSkeletonBounds(entity)
            entity[TransformPart::class].transform.setSize(skeletonBounds.size)
        }
    }

    private fun updateRootPosition(entity: Entity) {
        val skeleton = entity[SkeletonPart::class].skeleton
        val transform = entity[TransformPart::class].transform
        val newRootPosition: Vector2
        if (entity.has(BoundingSlotsPart::class)) {
            val skeletonBounds = getSkeletonBounds(entity)
            val rootYToMinYOffset = skeleton.rootBone.y - skeletonBounds.y
            newRootPosition = transform.bounds.base.add(0f, rootYToMinYOffset)
        } else {
            newRootPosition = transform.center
        }
        skeleton.rootBone.x = newRootPosition.x
        skeleton.rootBone.y = newRootPosition.y
        skeleton.updateWorldTransform()
    }

    private fun getSkeletonBounds(entity: Entity): Rectangle {
        val skeletonPart = entity[SkeletonPart::class]
        val boundingSlotNames = entity[BoundingSlotsPart::class].slotNames
        val boundingSlots = skeletonPart.skeleton.slots.filter { boundingSlotNames.contains(it.data.name) }
        return skeletonPart.skeleton.getBounds(boundingSlots)
    }
}