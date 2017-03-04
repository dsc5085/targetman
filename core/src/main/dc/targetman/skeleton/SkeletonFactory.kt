package dc.targetman.skeleton

import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.SkeletonBinary
import dclib.graphics.TextureCache
import dclib.system.io.FileUtils

class SkeletonFactory(private val textureCache: TextureCache) {
    fun create(skeletonPath: String, atlasName: String): Skeleton {
        val atlas = textureCache.getAtlas(atlasName)
        val skeletonBinary = SkeletonBinary(atlas)
        val skeletonFile = FileUtils.toFileHandle(skeletonPath)
        val skeleton = Skeleton(skeletonBinary.readSkeletonData(skeletonFile))
        skeleton.updateWorldTransform()
        return skeleton
    }
}