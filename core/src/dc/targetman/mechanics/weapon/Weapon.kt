package dc.targetman.mechanics.weapon

import dclib.util.FloatRange

class Weapon(val reloadTime: Float,
             val numBullets: Int,
             val spread: Float,
             minSpeed: Float,
             maxSpeed: Float,
             val recoil: Float,
             val bulletType: String) {
    val speedRange = FloatRange(minSpeed, maxSpeed)
}
