package dc.targetman.graphics

import dc.targetman.command.Executer
import dclib.epf.graphics.EntityDrawerManager

class DisableDrawerExecuter(private val manager: EntityDrawerManager) : Executer {
    override val verb = "drawer off"

    override fun execute(params: Map<String, String>) {
        val name = params.getValue("name")
        manager.disableDrawer(name)
    }
}