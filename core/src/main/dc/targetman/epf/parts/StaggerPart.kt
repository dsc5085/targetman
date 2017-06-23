package dc.targetman.epf.parts

import dc.targetman.mechanics.StaggerState
import dclib.util.Maths

class StaggerPart(val recoverySpeed: Float, val minStunAmount: Float, val minKnockdownAmount: Float) {
    var amount = 0f

    val state: StaggerState
        get() {
            val state: StaggerState
            if (amount < minStunAmount) {
                state = StaggerState.OK
            } else if (Maths.between(amount, minStunAmount, minKnockdownAmount)) {
                state = StaggerState.HURT
            } else {
                state = StaggerState.DOWN
            }
            return state
        }
}