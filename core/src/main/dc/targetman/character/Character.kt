package dc.targetman.character

data class Character(
        val skeletonPath: String = "",
        val limbs: List<CharacterLimb> = listOf(),
        val rotatorName: String = "",
        val muzzleName: String = "",
        val atlasName: String = "")