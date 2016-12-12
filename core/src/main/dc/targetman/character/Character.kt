package dc.targetman.character

import com.esotericsoftware.spine.Skeleton

data class Character(
        val skeleton: Skeleton,
        val limbs: List<Limb>,
        val rotatorName: String,
        val muzzleName: String,
        val atlasName: String)