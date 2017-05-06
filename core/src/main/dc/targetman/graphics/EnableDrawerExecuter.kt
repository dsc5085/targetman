package dc.targetman.graphics

import dc.targetman.command.Executer
import dclib.epf.graphics.EntityDrawerManager

class EnableDrawerExecuter(private val manager: EntityDrawerManager) : Executer {
    override val verb = "draw on"

    override fun execute(params: Map<String, String>) {
        val name = params.getValue("name")
        manager.enableDrawer(name)
    }
}