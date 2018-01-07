package dc.targetman.epf.parts

import dc.targetman.ai.AiProfile
import dc.targetman.ai.Path
import dc.targetman.ai.SteerState
import dclib.util.Timer

class AiPart(val profile: AiProfile) {
    private val THINK_TIME = 0.1f

    val path = Path()
    val steerState = SteerState()

    private val calculatePathTimer = Timer(THINK_TIME, THINK_TIME)

    fun checkCalculatePath(): Boolean {
        return calculatePathTimer.check()
    }

    fun tick(delta: Float) {
        calculatePathTimer.tick(delta)
    }
}
