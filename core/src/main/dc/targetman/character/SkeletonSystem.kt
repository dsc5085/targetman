package dc.targetman.character

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Slot
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart

class SkeletonSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    // TODO: Cleanup
    override fun update(delta: Float, entity: Entity) {
        val skeletonPart = entity.tryGet(SkeletonPart::class.java)
        if (skeletonPart != null) {
            val skeleton = skeletonPart.skeleton
            val center = entity[TransformPart::class.java].transform.center
            skeleton.rootBone.x = center.x
            skeleton.rootBone.y = center.y
            skeleton.updateWorldTransform()
            for (limb in skeletonPart.getActiveLimbs()) {
                // TODO: name should be included with limb
                val name = skeletonPart.getName(limb)
                val slot = skeleton.slots.singleOrNull { it.attachment.name == name }
                if (slot != null) {
                    updateLimbTransform(limb, slot)
                }
            }
        }
    }

    private fun updateBounds() {

    }

    private fun updateLimbTransform(limb: Entity, slot: Slot) {
        val bone = slot.bone
        val scale = Vector2(bone.worldScaleX, bone.worldScaleY)
        val transform = limb[TransformPart::class.java].transform
        val attachment = slot.attachment
        if (attachment is RegionAttachment) {
            transform.rotation = bone.worldRotationX + attachment.rotation
            val localOffset = Vector2(attachment.x, attachment.y).scl(scale).setAngle(bone.worldRotationX)
            val newGlobal = Vector2(bone.worldX, bone.worldY).add(localOffset)
            val origin = transform.size.scl(0.5f)
            transform.setGlobal(origin, newGlobal)
        }
    }
}