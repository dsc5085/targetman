package dc.targetman.screens

import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.viewport.ScreenViewport
import dc.targetman.command.CommandProcessor
import dclib.system.Input
import dclib.ui.StageUtils
import dclib.ui.UiPack

class ConsoleScreen(private val commandProcessor: CommandProcessor, private val uiPack: UiPack) : Screen {
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
        StageUtils.resize(stage, width, height)
    }

    override fun dispose() {
        input.dispose()
        stage.dispose()
    }

    private fun createStage(): Stage {
        val stage = Stage(ScreenViewport())
        val mainTable = createMainTable()
        stage.addActor(mainTable)
        return stage
    }

    private fun createMainTable(): Table {
        val mainTable = uiPack.table()
        mainTable.setFillParent(true)
        val textField = uiPack.textField()
        textField.setTextFieldListener(this::handleKeyTyped)
        mainTable.add(textField)
        return mainTable
    }

    private fun handleKeyTyped(textField: TextField, c: Char) {
        val isLineBreakChar = c == '\n' || c == '\r'
        if (isLineBreakChar) {
            commandProcessor.execute(textField.text)
            textField.clear()
        }
    }
}