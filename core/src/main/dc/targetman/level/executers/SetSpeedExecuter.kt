package dc.targetman.level.executers

import dc.targetman.command.Executer
import dclib.system.Advancer

class SetSpeedExecuter(private val advancer: Advancer) : Executer {
    override val verb = "set speed"

    override fun execute(params: Map<String, String>) {
        advancer.deltaScale = params.getValue("speed").toFloat()
    }
}
