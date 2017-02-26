package dc.targetman.epf.parts

import dc.targetman.skeleton.Limb

class FiringPart(val rotatorName: String, val muzzle: Limb) {
    var aimDirection = 0
    var aimRotation = 0f
    var triggered = false
}