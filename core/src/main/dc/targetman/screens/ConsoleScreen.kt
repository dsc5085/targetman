package dc.targetman.screens

import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.viewport.ScreenViewport
import dc.targetman.command.CommandProcessor
import dclib.ui.StageUtils
import dclib.ui.UiPack

class ConsoleScreen(private val commandProcessor: CommandProcessor, private val uiPack: UiPack) : Screen {
    private val stage = createStage()

    override fun hide() {
    }

    override fun show() {
    }

    override fun render(delta: Float) {
        stage.act(delta)
        draw()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(width: Int, height: Int) {
        StageUtils.resize(stage, width, height)
    }

    override fun dispose() {
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
        val textField = uiPack.textField()
        textField.setTextFieldListener(this::handleKeyTyped)
        mainTable.add(textField)
        return mainTable
    }

    private fun handleKeyTyped(textField: TextField, c: Char) {
        if (c == '\n') {
            commandProcessor.execute(textField.text)
            textField.clear()
        }
    }

    private fun draw() {
        stage.draw()
    }
}