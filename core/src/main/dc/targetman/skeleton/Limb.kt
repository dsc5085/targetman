package dc.targetman.skeleton

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.attachments.RegionAttachment
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.geometry.VectorUtils

class Limb(val bone: Bone, val entity: Entity) {
    var parent: Limb? = null
        private set
    val skeleton = bone.skeleton
    val name get() = bone.data.name
    val transform get() = entity[TransformPart::class].transform
    /**
     * Scale used for Spine bone manipulations.
     */
    val spineScale get() = Vector2(bone.worldScaleX, bone.worldScaleY).scl(flipScale)

    val flipScale: Vector2
        get() {
            val rootBoneScale = Vector2(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY)
            return VectorUtils.sign(rootBoneScale)
        }

    private val childLinks = mutableSetOf<LimbLink>()

    fun getRegionAttachment(): RegionAttachment? {
        // TODO: make a method to return just the attachment/bone's transform and rotation offsets?  thats all we need
        val slots = skeleton.slots.filter { it.bone.data.name == name }
        return SkeletonUtils.getRegionAttachments(slots).singleOrNull()
    }

    fun getLinks(vararg linkTypes: LinkType = LinkType.values()): Set<LimbLink> {
        return childLinks.filter { linkTypes.contains(it.type) }.toSet()
    }

    fun getChildren(vararg linkTypes: LinkType = LinkType.values()): Set<Limb> {
        return getLinks(*linkTypes).map { it.limb }.toSet()
    }

    // TODO: Return to not include this Limb in order for the method name to make more sense. Instead, create a getBranch method for that case
    fun getDescendants(vararg linkTypes: LinkType = LinkType.values()): Set<Limb> {
        val descendants = getChildren(*linkTypes).flatMap { it.getDescendants(*linkTypes) }
        return descendants.plus(this).toSet()
    }

    fun append(childLink: LimbLink) {
        childLinks.add(childLink)
        childLink.limb.parent = this
    }

    fun detach(limb: Limb): Boolean {
        limb.parent = null
        return childLinks.removeAll { it.limb === limb }
    }
}