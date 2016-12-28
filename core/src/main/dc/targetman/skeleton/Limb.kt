package dc.targetman.skeleton

import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.attachments.Attachment
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity

class Limb(val bone: Bone, val entity: Entity, val container: Entity) {
    val name: String
        get() = bone.data.name

    val isActive: Boolean
        get() = entity.isActive

    private val skeletonPart: SkeletonPart
        get() = container[SkeletonPart::class]

    fun getAttachments(): List<Attachment> {
        return skeletonPart.skeleton.slots.filter { it.bone.data.name == name }.map { it.attachment }
    }

    fun getChildren(): List<Limb> {
        return bone.children.map { skeletonPart[it.data.name] }
    }

    fun getDescendants(): List<Limb> {
        val descendants = bone.children.flatMap { skeletonPart[it.data.name].getDescendants() }
        return descendants.plus(this)
    }
}