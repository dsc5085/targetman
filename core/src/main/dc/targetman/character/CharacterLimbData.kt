package dc.targetman.character

import dc.targetman.physics.collision.Material

data class CharacterLimbData(
        val name: String = "",
        val health: Float = 100f,
        val material: Material = Material.FLESH,
        val isMovement: Boolean = false,
        val isVital: Boolean = false,
        val isPassive: Boolean = false)