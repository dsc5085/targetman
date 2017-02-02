package dc.targetman.skeleton

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.attachments.RegionAttachment
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.geometry.VectorUtils
import dclib.physics.Transform

class Limb(val bone: Bone, val entity: Entity) {
    val skeleton = bone.skeleton

    val isActive: Boolean
        get() = entity.isActive

    val name: String
        get() = bone.data.name

    val transform: Transform
        get() = entity[TransformPart::class].transform

    val scale: Vector2
        get() {
            val rootScale = Vector2(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY)
            val flipScale = VectorUtils.sign(rootScale)
            return Vector2(bone.worldScaleX, bone.worldScaleY).scl(flipScale)
        }

    private val children = mutableSetOf<Limb>()

    fun getRegionAttachment(): RegionAttachment? {
        // TODO: make a method to return just the attachment/bone's transform and rotation offsets?  thats all we need
        val slots = skeleton.slots.filter { it.bone.data.name == name }
        return SkeletonUtils.getRegionAttachments(slots).singleOrNull()
    }

    fun getChildren(includeInactive: Boolean = false): Set<Limb> {
        return children.filter { includeInactive || it.isActive }.toSet()
    }

    fun getDescendants(includeInactive: Boolean = false): Set<Limb> {
        val descendants = getChildren(includeInactive).flatMap { it.getDescendants(includeInactive).plus(it) }
        return descendants.toSet()
    }

    fun addChild(limb: Limb) {
        children.add(limb)
    }
}