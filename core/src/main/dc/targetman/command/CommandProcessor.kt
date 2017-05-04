package dc.targetman.command

class CommandProcessor {
    private val executers = mutableListOf<Executer<*>>()
    private val parser = CommandParser()

    fun add(executer: Executer<*>) {
        executers.add(executer)
    }

    fun execute(text: String) {
        val command = parser.parseCommand(text)
        val executer = executers.single { it.verb == command.verb }
        executer.execute(command.params)
    }
}