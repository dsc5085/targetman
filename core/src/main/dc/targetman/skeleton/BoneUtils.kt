package dc.targetman.skeleton

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import dclib.geometry.VectorUtils

object BoneUtils {
    fun getScale(bone: Bone): Vector2 {
        val skeleton = bone.skeleton
        val rootScale = Vector2(skeleton.rootBone.scaleX, skeleton.rootBone.scaleY)
        val flipScale = VectorUtils.sign(rootScale)
        return Vector2(bone.worldScaleX, bone.worldScaleY).scl(flipScale)
    }
}