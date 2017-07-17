package dc.targetman.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import dclib.graphics.ScreenHelper
import dclib.ui.FontSize
import dclib.ui.UiPack

class DebugView(
        private val uiPack: UiPack,
        private val spriteBatch: PolygonSpriteBatch,
        private val screenHelper: ScreenHelper,
        private val stage: Stage
) {
    private var isEnabled = true
    private lateinit var fpsLabel: Label

    init {
        stage.addActor(createMainTable())
    }

    fun toggleEnabled() {
        isEnabled = !isEnabled
        fpsLabel.isVisible = isEnabled
    }

    fun update() {
        fpsLabel.setText("${Gdx.graphics.framesPerSecond}")
    }

    fun draw() {
        if (isEnabled) {
            spriteBatch.projectionMatrix = stage.camera.combined
            spriteBatch.begin()
            val inputCoords = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            val cursorWorldCoords = screenHelper.toWorldCoords(inputCoords)
            val cursorText = "${cursorWorldCoords.x}, ${cursorWorldCoords.y}"
            val cursorDrawCoords = stage.screenToStageCoordinates(inputCoords)
            uiPack.getFont(FontSize.SMALL).draw(spriteBatch, cursorText, cursorDrawCoords.x, cursorDrawCoords.y)
            spriteBatch.end()
        }
    }

    private fun createMainTable(): Table {
        val mainTable = uiPack.table()
        mainTable.setFillParent(true)
        fpsLabel = uiPack.label("")
        mainTable.add(fpsLabel).expand().top().right()
        return mainTable
    }
}