package dc.targetman.graphics

import dc.targetman.command.Executer
import dclib.epf.graphics.DrawerManager

class DisableDrawerExecuter(private val manager: DrawerManager) : Executer {
    override val verb = "drawer off"

    override fun execute(params: Map<String, String>) {
        val name = params.getValue("name")
        manager.disableDrawer(name)
    }
}