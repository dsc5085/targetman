package dc.targetman.mechanics.weapon

import dclib.util.FloatRange

data class Weapon(
        val reloadTime: Float = 1f,
        val numBullets: Int = 1,
        val spread: Float = 0f,
        val minSpeed: Float = 1f,
        val maxSpeed: Float = 1f,
        val recoil: Float = 0f,
        val width: Float = 1f,
        val regionName: String = "",
        var bullet: Bullet = Bullet()
) {
    val speedRange: FloatRange
        get() = FloatRange(minSpeed, maxSpeed)
}
