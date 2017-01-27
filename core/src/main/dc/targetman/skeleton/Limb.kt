package dc.targetman.skeleton

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.geometry.VectorUtils
import dclib.physics.Transform

class Limb(val name: String, val entity: Entity, private val skeletonPart: SkeletonPart) {
    val isActive: Boolean
        get() = entity.isActive

    val bone: Bone
        get() = skeletonPart.skeleton.findBone(name)

    val transform: Transform
        get() = entity[TransformPart::class].transform

    val scale: Vector2
        get() {
            val skeleton = bone.skeleton
            val rootScale = Vector2(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY)
            val flipScale = VectorUtils.sign(rootScale)
            return Vector2(bone.worldScaleX, bone.worldScaleY).scl(flipScale)
        }

    fun getRegionAttachment(): RegionAttachment? {
        // TODO: make a method to return just the attachment/bone's transform and rotation offsets?  thats all we need
        val attachments = skeletonPart.skeleton.slots.filter { it.bone.data.name == name }.map { it.attachment }
        return attachments.filterIsInstance<RegionAttachment>().singleOrNull()
    }

    fun getChildren(): Set<Limb> {
        return bone.children.map { skeletonPart[it.data.name] }.filter { it.isActive }.toSet()
    }

    fun getDescendants(): Set<Limb> {
        val descendants = getChildren().flatMap { it.getDescendants().plus(it) }
        return descendants.toSet()
    }
}