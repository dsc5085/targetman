package dc.targetman.character

import dc.targetman.mechanics.weapon.WeaponData

data class Character(
        val skeletonPath: String = "",
        val limbDatas: List<CharacterLimbData> = listOf(),
        val rotatorName: String = "",
        val gripperName: String = "gripper",
        val muzzleName: String = "muzzle",
        val atlasName: String = "",
        val health: Float = 1f,
        val stunResist: Float = 50f,
        val weaponData: WeaponData = WeaponData())