package dc.targetman.character

import dc.targetman.mechanics.weapon.Weapon

data class Character(
        val skeletonPath: String = "",
        val limbs: List<CharacterLimb> = listOf(),
        val rotatorName: String = "",
        val muzzleName: String = "",
        val atlasName: String = "",
        val weapon: Weapon = Weapon())