package dc.targetman.epf.parts

import com.badlogic.gdx.math.Vector2

class FiringPart(val rotatorName: String, val muzzleName: String) {
    var targetCoord = Vector2()
    var aimAccelerationTime = 0f
    var lastAimDelta = 0f
}