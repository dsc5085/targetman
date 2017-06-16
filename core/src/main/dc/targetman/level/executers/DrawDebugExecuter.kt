package dc.targetman.level.executers

import dc.targetman.command.Executer
import dc.targetman.screens.LevelScreen

class DrawDebugExecuter(private val screen: LevelScreen) : Executer {
    override val verb = "draw debug"

    override fun execute(params: Map<String, String>) {
        screen.toggleDebugView()
    }
}