package dc.targetman.command

import dclib.eventing.EventDelegate

class CommandProcessor {
    val commandExecuted = EventDelegate<CommandExecutedEvent>()

    private val modules = mutableListOf<CommandModule>()
    private val parser = CommandParser()

    fun add(module: CommandModule) {
        module.disposed.on { modules.remove(module) }
        modules.add(module)
    }

    fun execute(text: String) {
        val command = parser.parseCommand(text)
        val executers = modules.flatMap { it.executers }
        for (executer in executers) {
            executer.execute(command.params)
        }
        commandExecuted.notify(CommandExecutedEvent(text))
    }
}