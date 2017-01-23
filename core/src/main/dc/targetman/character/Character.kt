package dc.targetman.character

import dc.targetman.mechanics.weapon.WeaponData

data class Character(
        val skeletonPath: String = "",
        val limbs: List<CharacterLimb> = listOf(),
        val rotatorName: String = "",
        val muzzleName: String = "",
        val atlasName: String = "",
        val health: Float = 1f,
        val weaponData: WeaponData = WeaponData())