package dc.targetman.epf.parts

import dclib.physics.Transform
import dclib.util.Timer

class StaggerPart(val minForce: Float, staggerTime: Float) {
    var isStaggered = false
    val staggerTimer = Timer(staggerTime)
    val oldLimbTransforms = mutableMapOf<String, Transform>()
}