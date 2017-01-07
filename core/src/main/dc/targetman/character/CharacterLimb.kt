package dc.targetman.character

import dc.targetman.physics.collision.Material

data class CharacterLimb(
        val name: String = "",
        val health: Float = 0f,
        val material: Material = Material.FLESH,
        val isMovement: Boolean = false,
        val isVital: Boolean = false)