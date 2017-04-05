package dc.targetman.epf.parts

import dc.targetman.mechanics.StaggerState
import dclib.physics.Transform
import dclib.util.Maths

class StaggerPart(val recoverySpeed: Float, val minStunAmount: Float, val minKnockdownAmount: Float) {
    var amount = 0f
    val oldLimbTransforms = mutableMapOf<String, Transform>()

    val state: StaggerState
        get() {
            val state: StaggerState
            if (amount < minStunAmount) {
                state = StaggerState.OK
            } else if (Maths.between(amount, minStunAmount, minKnockdownAmount)) {
                state = StaggerState.STUNNED
            } else {
                state = StaggerState.DOWN
            }
            return state
        }
}