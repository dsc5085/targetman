package dc.targetman.graphics

import dc.targetman.command.Executer
import dclib.epf.graphics.DrawerManager

class EnableDrawerExecuter(private val manager: DrawerManager) : Executer {
    override val verb = "drawer on"

    override fun execute(params: Map<String, String>) {
        val name = params.getValue("name")
        manager.enableDrawer(name)
    }
}