package dc.targetman.skeleton

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.SkeletonBinary
import com.esotericsoftware.spine.attachments.RegionAttachment
import dclib.geometry.VectorUtils
import dclib.geometry.abs
import dclib.system.io.FileUtils

object SkeletonUtils {
    fun createSkeleton(skeletonPath: String, atlas: TextureAtlas): Skeleton {
        val skeletonBinary = SkeletonBinary(atlas)
        val skeletonFile = FileUtils.toFileHandle(skeletonPath)
        val skeleton = Skeleton(skeletonBinary.readSkeletonData(skeletonFile))
        skeleton.updateWorldTransform()
        return skeleton
    }

    fun getOffset(from: Bone, to: RegionAttachment, toScale: Vector2): Vector2 {
        // TODO: see if its better to calculate scale or pass it in, e.g. toScale
        val attachmentScale = calculateAttachmentScale(toScale, to.rotation)
        val offsetRotation = VectorUtils.getScaledRotation(from.worldRotationX, toScale)
        return Vector2(to.x, to.y).rotate(offsetRotation).scl(attachmentScale)
    }

    fun calculateAttachmentScale(boneScale: Vector2, attachmentRotation: Float): Vector2 {
        val boneFlipScale = VectorUtils.sign(boneScale)
        val matrix = Matrix3().rotate(attachmentRotation).scale(boneScale).rotate(-attachmentRotation)
        return matrix.getScale(Vector2()).abs().scl(boneFlipScale)
    }

    /**
     * @param size object's size
     * @return origin used by Spine runtimes, which is the object's local center
     */
    fun getOrigin(size: Vector2): Vector2 {
        return size.cpy().scl(0.5f)
    }
}