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

    fun execute(commandText: String) {
        val command = parser.parseCommand(commandText)
        // TODO: Ensure only one executer is found?
        val executers = modules.flatMap { it.executers }.filter { it.verb == command.verb }
        val text = StringBuilder()
        if (executers.isEmpty()) {
            text.append("\"${commandText}\" is an invalid command")
        }
        for (executer in executers) {
            try {
                executer.execute(command.params)
                text.append(commandText)
            } catch (ex: Exception) {
                text.append("${executer::class.simpleName} error: $ex")
            }
        }
        commandExecuted.notify(CommandExecutedEvent(text.toString()))
    }
}