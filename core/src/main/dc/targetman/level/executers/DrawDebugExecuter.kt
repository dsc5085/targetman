package dc.targetman.level.executers

import dc.targetman.command.Executer
import dc.targetman.level.DebugView

class DrawDebugExecuter(private val debugView: DebugView) : Executer {
    override val verb = "draw debug"

    override fun execute(params: Map<String, String>) {
        debugView.toggleEnabled()
    }
}