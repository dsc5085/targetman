package dc.targetman.character

import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.SkeletonBinary
import dc.targetman.physics.collision.Material
import dclib.graphics.TextureCache
import dclib.system.io.FileUtils

// TODO: Load the literal values from a file
class CharacterLoader(private val pixelsPerUnit: Float, private val textureCache: TextureCache) {
    fun create(skeletonPath: String): Character {
        val atlasName = "skins/man"
        val atlas = textureCache.getAtlas(atlasName)
        val skeletonBinary = SkeletonBinary(atlas)
        skeletonBinary.scale = 1 / pixelsPerUnit
        val skeletonFile = FileUtils.internalPathToFileHandle(skeletonPath)
        val skeleton = Skeleton(skeletonBinary.readSkeletonData(skeletonFile))
        return Character(skeleton, createLimbs(), "right_bicep", "muzzle", atlasName)
    }

    private fun createLimbs(): List<Limb> {
        return listOf(
                Limb("head", 50f, Material.FLESH, isVital = true),
                Limb("neck", 50f, Material.FLESH),
                Limb("torso", 100f, Material.FLESH, isVital = true),
                Limb("gun", 1000f, Material.METAL),
                Limb("right_hand", 50f, Material.FLESH),
                Limb("right_forearm", 50f, Material.FLESH),
                Limb("right_bicep", 50f, Material.FLESH),
                Limb("right_foot", 50f, Material.FLESH, true),
                Limb("right_shin", 75f, Material.FLESH, true),
                Limb("right_thigh", 75f, Material.FLESH),
                Limb("left_thigh", 75f, Material.FLESH),
                Limb("left_shin", 75f, Material.FLESH, true),
                Limb("left_foot", 50f, Material.FLESH, true),
                Limb("left_bicep", 50f, Material.FLESH),
                Limb("left_forearm", 50f, Material.FLESH),
                Limb("left_hand", 50f, Material.FLESH)
        )
    }
}