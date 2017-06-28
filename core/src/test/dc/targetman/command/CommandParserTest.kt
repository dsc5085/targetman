package dc.targetman.command

import org.junit.Test

// TODO: Add more tests
class CommandParserTest {
    private val parser = CommandParser()

    @Test
    fun parseCommand() {
        val command = parser.parseCommand("open file -path \"C:/david files/poop.txt\" -what jee")
    }
}