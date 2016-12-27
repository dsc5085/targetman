package dc.targetman.epf.parts

import dc.targetman.mechanics.weapon.Weapon
import dclib.util.Timer

class WeaponPart(val weapon: Weapon, val rotatorName: String, val muzzleName: String) {
    var aimDirection = 0
    var aimRotation = 0f

    private val reloadTimer = Timer(weapon.reloadTime, weapon.reloadTime)
    private var triggered = false

    fun shouldFire(): Boolean {
        return triggered && reloadTimer.isElapsed
    }

    fun reset() {
        reloadTimer.reset()
    }

    fun setTriggered(triggered: Boolean) {
        this.triggered = triggered
    }

    fun update(delta: Float) {
        reloadTimer.tick(delta)
    }
}