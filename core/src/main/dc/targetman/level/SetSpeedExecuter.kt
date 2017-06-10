package dc.targetman.level

import dc.targetman.command.Executer
import dclib.system.Advancer

class SetSpeedExecuter(private val advancer: Advancer) : Executer {
    override val verb = "set speed"

    override fun execute(params: Map<String, String>) {
        advancer.speed = params.getValue("speed").toFloat()
    }
}
