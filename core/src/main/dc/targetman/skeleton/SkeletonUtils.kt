package dc.targetman.skeleton

import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.attachments.RegionAttachment
import dclib.geometry.VectorUtils
import dclib.geometry.abs

object SkeletonUtils {
    fun getScale(bone: Bone): Vector2 {
        val skeleton = bone.skeleton
        val rootScale = Vector2(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY)
        val flipScale = VectorUtils.sign(rootScale)
        return Vector2(bone.worldScaleX, bone.worldScaleY).scl(flipScale)
    }

    fun getOffset(from: Bone, to: RegionAttachment, toScale: Vector2): Vector2 {
        // TODO: see if its better to calculate scale or pass it in, e.g. toScale
//        val boneScale = getScale(from)
        val attachmentScale = calculateAttachmentScale(toScale, to.rotation)
        val offsetRotation = getScaledRotation(from.worldRotationX, toScale)
        return Vector2(to.x, to.y).rotate(offsetRotation).scl(attachmentScale)
    }

    fun calculateAttachmentScale(boneScale: Vector2, attachmentRotation: Float): Vector2 {
        val boneFlipScale = VectorUtils.sign(boneScale)
        val matrix = Matrix3().rotate(attachmentRotation).scale(boneScale).rotate(-attachmentRotation)
        return matrix.getScale(Vector2()).abs().scl(boneFlipScale)
    }

    fun getScaledRotation(degrees: Float, scale: Vector2): Float {
        return VectorUtils.toVector2(degrees, 1f).scl(scale).angle()
    }

    /**
     * @param size object's size
     * @return origin used by Spine runtimes, which is the object's local center
     */
    fun getOrigin(size: Vector2): Vector2 {
        return size.cpy().scl(0.5f)
    }
}