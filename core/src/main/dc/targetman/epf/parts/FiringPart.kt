package dc.targetman.epf.parts

import com.badlogic.gdx.math.Vector2

class FiringPart(val rotatorName: String, val muzzleName: String) {
    var targetCoord = Vector2()
    var aimAngle = 0f
    var triggered = false
}