package dc.targetman.skeleton

import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.physics.Transform

class Limb(val bone: Bone, val entity: Entity, val container: Entity) {
    val name: String
        get() = bone.data.name

    val isActive: Boolean
        get() = entity.isActive

    val transform: Transform
        get() = entity[TransformPart::class].transform

    private val skeletonPart: SkeletonPart
        get() = container[SkeletonPart::class]

    fun getRegionAttachment(): RegionAttachment? {
        // TODO: make a method to return just the attachment/bone's transform and rotation offsets?  thats all we need
        val attachments = skeletonPart.skeleton.slots.filter { it.bone.data.name == name }.map { it.attachment }
        return attachments.filterIsInstance<RegionAttachment>().singleOrNull()
    }

    fun getChildren(): List<Limb> {
        return bone.children.map { skeletonPart[it.data.name] }
    }

    fun getDescendants(): List<Limb> {
        val descendants = bone.children.flatMap { skeletonPart[it.data.name].getDescendants() }
        return descendants.plus(this)
    }
}