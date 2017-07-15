package dc.targetman.level.executers

import dc.targetman.command.Executer
import dclib.system.Advancer

class StepExecuter(private val advancer: Advancer) : Executer {
    override val verb = "step"

    override fun execute(params: Map<String, String>) {
        val stepAmount = params.getValue("amount").toFloat()
        advancer.update(stepAmount)
    }
}
