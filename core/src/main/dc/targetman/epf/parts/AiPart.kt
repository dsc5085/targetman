package dc.targetman.epf.parts

import dc.targetman.ai.AiProfile
import dc.targetman.ai.DefaultNode
import dclib.util.Timer

class AiPart(val profile: AiProfile) {
    companion object {
        private val THINK_TIME = 0.1f
    }

    var path = listOf<DefaultNode>()

    private val updatePathTimer = Timer(THINK_TIME, THINK_TIME)

    fun checkUpdatePath(): Boolean {
        return updatePathTimer.check()
    }

    fun tick(delta: Float) {
        updatePathTimer.tick(delta)
    }
}
