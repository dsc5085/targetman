package dc.targetman.skeleton

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.attachments.RegionAttachment
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.geometry.VectorUtils

class Limb(val bone: Bone, val entity: Entity) {
    val skeleton = bone.skeleton
    val isActive get() = entity.isActive
    val name get() = bone.data.name
    val transform get() = entity[TransformPart::class].transform
    val scale get() = Vector2(bone.worldScaleX, bone.worldScaleY).scl(flipScale)

    val flipScale: Vector2
        get() {
            val rootBoneScale = Vector2(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY)
            return VectorUtils.sign(rootBoneScale)
        }

    private val children = mutableSetOf<Limb>()
    // TODO: Merge skeleton links list with children list
    private val skeletonLinks = mutableSetOf<SkeletonRoot>()

    fun getRegionAttachment(): RegionAttachment? {
        // TODO: make a method to return just the attachment/bone's transform and rotation offsets?  thats all we need
        val slots = skeleton.slots.filter { it.bone.data.name == name }
        return SkeletonUtils.getRegionAttachments(slots).singleOrNull()
    }

    fun getChildren(includeInactive: Boolean = false, includeLinked: Boolean = false): Set<Limb> {
        val allChildren = children.toMutableList()
        if (includeLinked) {
            allChildren.addAll(skeletonLinks.map { it.limb })
        }
        return allChildren.filter { includeInactive || it.isActive }.toSet()
    }

    fun getDescendants(includeInactive: Boolean = false, includeLinked: Boolean = false): Set<Limb> {
        val descendants = getChildren(includeInactive, includeLinked)
                .flatMap { it.getDescendants(includeInactive, includeLinked) }
        return descendants.plus(this).toSet()
    }

    fun addChild(limb: Limb) {
        children.add(limb)
    }

    fun removeChild(limb: Limb) {
        children.remove(limb)
        skeletonLinks.removeAll { it.limb === limb }
    }

    fun getSkeletonLinks(): Set<SkeletonRoot> {
        return skeletonLinks.toSet()
    }

    fun add(skeletonLink: SkeletonRoot) {
        skeletonLinks.add(skeletonLink)
    }
}