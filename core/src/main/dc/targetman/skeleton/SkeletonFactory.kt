package dc.targetman.skeleton

import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.SkeletonBinary
import com.esotericsoftware.spine.SkeletonJson
import dclib.graphics.TextureCache
import dclib.system.io.FileUtils

class SkeletonFactory(private val textureCache: TextureCache) {
    fun create(skeletonPath: String, atlasName: String): Skeleton {
        val atlas = textureCache.getAtlas(atlasName)
        val skeletonFile = FileUtils.toFileHandle(skeletonPath)
        val skeletonData = when (skeletonFile.extension()) {
            "skel" -> SkeletonBinary(atlas).readSkeletonData(skeletonFile)
            "json" -> SkeletonJson(atlas).readSkeletonData(skeletonFile)
            else -> throw IllegalArgumentException("Unknown skeleton file extension: ${skeletonPath}")
        }
        val skeleton = Skeleton(skeletonData)
        skeleton.updateWorldTransform()
        return skeleton
    }
}