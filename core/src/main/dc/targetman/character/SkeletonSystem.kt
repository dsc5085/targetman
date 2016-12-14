package dc.targetman.character

import com.badlogic.gdx.math.Vector2
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
            skeleton.rootBone.x = 0f
            skeleton.rootBone.y = 0f
            skeleton.updateWorldTransform()
            for (limb in skeletonPart.getActiveLimbs()) {
                // TODO: name should be included with limb
                val name = skeletonPart.getName(limb)
                val slot = skeleton.slots.singleOrNull { it.attachment.name == name }
                if (slot != null) {
                    val bone = slot.bone
                    val scale = Vector2(bone.worldScaleX, bone.worldScaleY)
                    val transform = limb[TransformPart::class.java].transform
                    transform.position = Vector2(bone.worldX, bone.worldY)
//                    transform.rotation = bone.rotation
                }
            }
        }
    }
}