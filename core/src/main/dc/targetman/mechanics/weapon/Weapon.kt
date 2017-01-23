package dc.targetman.mechanics.weapon

import dclib.util.FloatRange
import dclib.util.Timer

class Weapon(val data: WeaponData) {
    val reloadTimer = Timer(data.reloadTime, data.reloadTime)
    val speedRange = FloatRange(data.minSpeed, data.maxSpeed)
}