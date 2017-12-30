package dc.targetman.epf.parts

import dc.targetman.mechanics.StaggerState

class StaggerPart(val recoverySpeed: Float, val stunResist: Float, val knockDownResist: Float) {
    var amount = 0f

    val state: StaggerState
        get() {
            val state: StaggerState
            if (amount < stunResist) {
                state = StaggerState.OK
            } else if (amount in stunResist..knockDownResist) {
                state = StaggerState.HURT
            } else {
                state = StaggerState.DOWN
            }
            return state
        }
}