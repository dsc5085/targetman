package dc.targetman.level.executers

import dc.targetman.command.Executer
import dc.targetman.screens.LevelScreen

class RestartExecuter(private val screen: LevelScreen) : Executer {
    override val verb = "restart"

    override fun execute(params: Map<String, String>) {
        screen.restart()
    }
}