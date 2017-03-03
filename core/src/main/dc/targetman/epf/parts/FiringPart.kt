package dc.targetman.epf.parts

class FiringPart(val rotatorName: String, val muzzleName: String) {
    var aimDirection = 0
    var aimRotation = 0f
    var triggered = false
}