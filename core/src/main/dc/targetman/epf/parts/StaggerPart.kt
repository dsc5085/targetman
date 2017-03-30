package dc.targetman.epf.parts

import dclib.util.Timer

class StaggerPart(val minForce: Float, staggerTime: Float) {
    var isStaggered = false
    val staggerTimer = Timer(staggerTime)
}