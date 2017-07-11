package dc.targetman.epf.parts

import dc.targetman.mechanics.StaggerState
import dclib.util.Maths

class StaggerPart(val recoverySpeed: Float, val stunResist: Float, val knockDownResist: Float) {
    var amount = 0f

    val state: StaggerState
        get() {
            val state: StaggerState
            if (amount < stunResist) {
                state = StaggerState.OK
            } else if (Maths.between(amount, stunResist, knockDownResist)) {
                state = StaggerState.HURT
            } else {
                state = StaggerState.DOWN
            }
            return state
        }
}