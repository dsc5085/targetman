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
        val executer = modules.flatMap { it.executers }.singleOrNull { it.verb == command.verb }
        val textBuilder = StringBuilder()
        if (executer == null) {
            textBuilder.append("\"${commandText}\" is an invalid command")
        } else {
            textBuilder.append(commandText)
            execute(command, executer, textBuilder)
        }
        commandExecuted.notify(CommandExecutedEvent(textBuilder.toString()))
    }

    private fun execute(command: Command, executer: Executer, textBuilder: StringBuilder) {
        try {
            executer.execute(command.params)
        } catch (ex: Exception) {
            textBuilder.appendln()
            textBuilder.append("Error: ${ex.message}")
        }
    }
}