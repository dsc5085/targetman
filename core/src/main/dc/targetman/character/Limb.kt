package dc.targetman.character

import dc.targetman.physics.collision.Material

data class Limb(
        val name: String,
        val health: Float,
        val material: Material,
        val isMovement: Boolean = false,
        val isVital: Boolean = false)