package dc.targetman.mechanics.weapon

import com.badlogic.gdx.math.Vector2

data class Bullet(
        val size: Vector2 = Vector2(),
        val regionName: String = "",
        val gravityScale: Float = 0.1f,
        val deathTime: Float = 10f,
        val damage: Float = 1f,
        val force: Float = 1f,
        val scaleTime: Float? = null
)