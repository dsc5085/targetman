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
        val textBuilder = StringBuilder()
        if (executers.isEmpty()) {
            textBuilder.append("\"${commandText}\" is an invalid command")
        } else {
            textBuilder.append(commandText)
            execute(command, executers, textBuilder)
        }
        commandExecuted.notify(CommandExecutedEvent(textBuilder.toString()))
    }

    private fun execute(command: Command, executers: List<Executer>, textBuilder: StringBuilder) {
        for (executer in executers) {
            try {
                executer.execute(command.params)
            } catch (ex: Exception) {
                textBuilder.appendln()
                textBuilder.append("Error: ${ex.message}")
            }
        }
    }
}