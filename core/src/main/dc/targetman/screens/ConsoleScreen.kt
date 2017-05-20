package dc.targetman.screens

import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.viewport.ScreenViewport
import dc.targetman.command.CommandExecutedEvent
import dc.targetman.command.CommandProcessor
import dclib.eventing.DefaultEvent
import dclib.eventing.EventDelegate
import dclib.system.Input
import dclib.ui.FontSize
import dclib.ui.UiPack
import dclib.ui.UiUtils

// TODO: Taking input even while inactive
class ConsoleScreen(
        private val commandProcessor: CommandProcessor,
        private val uiPack: UiPack
) : Screen {
    private val shown = EventDelegate<DefaultEvent>()

    private val input = Input()
    private val stage = createStage()
    private var isShown = false

    init {
        input.add(stage)
    }

    override fun hide() {
        isShown = false
    }

    override fun show() {
        isShown = true
        shown.notify(DefaultEvent())
    }

    override fun render(delta: Float) {
        if (isShown) {
            stage.act(delta)
            stage.draw()
        }
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun dispose() {
        input.dispose()
        stage.dispose()
    }

    private fun createStage(): Stage {
        val stage = Stage(ScreenViewport())
        stage.addActor(createMainTable())
        stage.setDebugAll(true)
        return stage
    }

    private fun createMainTable(): Table {
        val mainTable = uiPack.table()
        mainTable.setBackground("default-pane")
        mainTable.setFillParent(true)
        mainTable.add(createMainScrollPane()).expand().fill()
        return mainTable
    }

    private fun createMainScrollPane(): ScrollPane {
        val innerTable = uiPack.table().top()
        val historyLabel = uiPack.label("", FontSize.SMALL)
        historyLabel.setWrap(true)
        innerTable.add(historyLabel).expandX().fillX().row()
        innerTable.add(createCommandField()).expandX().fillX().top().row()
        val scrollPane = uiPack.scrollPane(innerTable)
        scrollPane.setSmoothScrolling(false)
        commandProcessor.commandExecuted.on { handleCommandExecuted(historyLabel, it, scrollPane) }
        return scrollPane
    }

    private fun createCommandField(): TextField {
        val commandField = uiPack.textField(FontSize.SMALL)
        UiUtils.hideBackground(commandField)
        commandField.setTextFieldListener(this::handleKeyTyped)
        shown.on { stage.keyboardFocus = commandField }
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
}