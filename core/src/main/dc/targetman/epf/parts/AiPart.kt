package dc.targetman.epf.parts

import dc.targetman.ai.AiProfile
import dc.targetman.ai.graph.DefaultNode
import dclib.util.Timer

class AiPart(val profile: AiProfile) {
    private val THINK_TIME = 0.1f

    var path = listOf<DefaultNode>()

    private val calculatePathTimer = Timer(THINK_TIME, THINK_TIME)

    fun checkCalculatePath(): Boolean {
        return calculatePathTimer.check()
    }

    fun tick(delta: Float) {
        calculatePathTimer.tick(delta)
    }
}
