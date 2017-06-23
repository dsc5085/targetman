package dc.targetman.skeleton

import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.Slot
import com.esotericsoftware.spine.attachments.RegionAttachment
import dclib.geometry.VectorUtils
import dclib.geometry.abs
import dclib.geometry.div
import dclib.geometry.size

object SkeletonUtils {
    fun getRegionAttachments(slots: Iterable<Slot>): List<RegionAttachment> {
        return slots.map { it.attachment }.filterIsInstance<RegionAttachment>()
    }

    fun getOffset(from: Bone, to: RegionAttachment, toScale: Vector2): Vector2 {
        // TODO: see if its better to calculate scale or pass it in, e.g. toScale
        val attachmentScale = calculateAttachmentScale(toScale, to.rotation)
        val offsetRotation = VectorUtils.getScaledRotation(from.worldRotationX, toScale)
        return Vector2(to.x, to.y).rotate(offsetRotation).scl(attachmentScale)
    }

    fun calculateRootScale(skeleton: Skeleton, size: Vector2): Vector2 {
        return size.div(skeleton.getBounds().size).scl(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY)
    }

    fun calculateAttachmentScale(boneScale: Vector2, attachmentRotation: Float): Vector2 {
        val boneFlipScale = VectorUtils.sign(boneScale)
        val matrix = Matrix3().rotate(attachmentRotation).scale(boneScale).rotate(-attachmentRotation)
        return matrix.getScale(Vector2()).abs().scl(boneFlipScale)
    }

    fun setWorldRotationX(bone: Bone, worldRotationX: Float) {
        bone.rotation += worldRotationX - bone.worldRotationX
    }
}