package dc.targetman.character

import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.SkeletonBinary
import dc.targetman.physics.collision.Material
import dclib.graphics.TextureCache
import dclib.system.io.FileUtils

// TODO: Load the literal values from a file
class CharacterLoader(private val textureCache: TextureCache) {
    fun create(skeletonPath: String): Character {
        val atlasName = "skins/man"
        val atlas = textureCache.getAtlas(atlasName)
        val skeletonBinary = SkeletonBinary(atlas)
        val skeletonFile = FileUtils.toFileHandle(skeletonPath)
        val skeleton = Skeleton(skeletonBinary.readSkeletonData(skeletonFile))
        skeleton.updateWorldTransform()
        return Character(skeleton, createLimbs(), "right_bicep", "muzzle", atlasName)
    }

    private fun createLimbs(): List<CharacterLimb> {
        return listOf(
                CharacterLimb("head", 50f, Material.FLESH, isVital = true),
                CharacterLimb("neck", 50f, Material.FLESH),
                CharacterLimb("torso", 100f, Material.FLESH, isVital = true),
                CharacterLimb("gun", 1000f, Material.METAL),
                CharacterLimb("right_hand", 50f, Material.FLESH),
                CharacterLimb("right_forearm", 50f, Material.FLESH),
                CharacterLimb("right_bicep", 50f, Material.FLESH),
                CharacterLimb("right_foot", 50f, Material.FLESH, true),
                CharacterLimb("right_shin", 50f, Material.FLESH, true),
                CharacterLimb("right_thigh", 50f, Material.FLESH),
                CharacterLimb("left_thigh", 50f, Material.FLESH),
                CharacterLimb("left_shin", 75f, Material.FLESH, true),
                CharacterLimb("left_foot", 50f, Material.FLESH, true),
                CharacterLimb("left_bicep", 50f, Material.FLESH),
                CharacterLimb("left_forearm", 50f, Material.FLESH),
                CharacterLimb("left_hand", 50f, Material.FLESH))
    }
}