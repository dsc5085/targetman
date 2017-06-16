package dc.targetman.screens

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import dc.targetman.command.CommandExecutedEvent
import dc.targetman.command.CommandProcessor
import dclib.eventing.DefaultEvent
import dclib.eventing.EventDelegate
import dclib.system.Screen
import dclib.ui.FontSize
import dclib.ui.UiPack
import dclib.ui.UiUtils

class ConsoleScreen(
        private val commandProcessor: CommandProcessor,
        private val uiPack: UiPack
) : Screen() {
    val closed = EventDelegate<DefaultEvent>()

    init {
        stage.addActor(createMainTable())
        add(ScreenInputAdapter())
    }

    fun createMainTable(): Table {
        val mainTable = uiPack.table()
        mainTable.setBackground("default-pane")
        mainTable.setFillParent(true)
        mainTable.add(createMainScrollPane(stage)).expand().fill()
        return mainTable
    }

    private fun createMainScrollPane(stage: Stage): ScrollPane {
        val innerTable = uiPack.table().top()
        val historyLabel = uiPack.label("", FontSize.SMALL)
        historyLabel.setWrap(true)
        innerTable.add(historyLabel).expandX().fillX().row()
        val commandField = createCommandField()
        innerTable.add(commandField).expandX().fillX().top().row()
        stage.keyboardFocus = commandField
        val scrollPane = uiPack.scrollPane(innerTable)
        scrollPane.setSmoothScrolling(false)
        commandProcessor.commandExecuted.on { handleCommandExecuted(historyLabel, it, scrollPane) }
        return scrollPane
    }

    private fun createCommandField(): TextField {
        val commandField = uiPack.textField(FontSize.SMALL)
        UiUtils.hideBackground(commandField)
        commandField.setTextFieldListener(this::handleKeyTyped)
        return commandField
    }

    private fun handleKeyTyped(textField: TextField, c: Char) {
        val isLineBreakChar = c == '\n' || c == '\r'
        if (isLineBreakChar) {
            commandProcessor.execute(textField.text)
            textField.text = ""
        }
    }

    private fun handleCommandExecuted(historyLabel: Label, event: CommandExecutedEvent, scrollPane: ScrollPane) {
        if (historyLabel.text.isNotEmpty()) {
            historyLabel.text.appendln()
        }
        historyLabel.text.append(event.text)
        historyLabel.invalidateHierarchy()
        scrollPane.validate()
        scrollPane.scrollPercentY = 1f
    }

    private inner class ScreenInputAdapter : InputAdapter() {
        override fun keyUp(keycode: Int): Boolean {
            when (keycode) {
                com.badlogic.gdx.Input.Keys.ESCAPE -> {
                    closed.notify(DefaultEvent())
                    return true
                }
            }
            return false
        }
    }
}