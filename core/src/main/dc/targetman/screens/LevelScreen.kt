package dc.targetman.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.viewport.Viewport
import dc.targetman.command.CommandModule
import dc.targetman.command.CommandProcessor
import dc.targetman.level.LevelController
import dc.targetman.level.executers.DrawDebugExecuter
import dc.targetman.level.executers.RestartExecuter
import dclib.eventing.DefaultEvent
import dclib.eventing.EventDelegate
import dclib.graphics.ScreenHelper
import dclib.graphics.TextureCache
import dclib.system.Screen
import dclib.ui.FontSize
import dclib.ui.UiPack

class LevelScreen(
        private val commandProcessor: CommandProcessor,
        private val textureCache: TextureCache,
        private val spriteBatch: PolygonSpriteBatch,
        private val shapeRenderer: ShapeRenderer,
        val pixelsPerUnit: Float,
        private val uiPack: UiPack
) : Screen() {
    val paused = EventDelegate<DefaultEvent>()

    private val camera = OrthographicCamera()
    private val viewport = createViewport(pixelsPerUnit)
    private val screenHelper = ScreenHelper(pixelsPerUnit, viewport)
    private lateinit var controller: LevelController
    private val commandModule: CommandModule
    private lateinit var fpsLabel: Label
    private var drawDebug = true

    init {
        setupController()
        add(LevelInputAdapter())
        commandModule = createCommandModule()
        commandProcessor.add(commandModule)
        stage.addActor(createMainTable())
    }

    fun toggleDebugView() {
        drawDebug = !drawDebug
        fpsLabel.isVisible = drawDebug
    }

    fun restart() {
        controller.dispose()
        setupController()
    }

    override fun update(delta: Float) {
        controller.update(delta)
        fpsLabel.setText("${Gdx.graphics.framesPerSecond}")
    }

    override fun draw() {
        controller.draw()
        if (drawDebug) {
            drawDebug()
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun dispose() {
        commandModule.dispose()
        controller.dispose()
    }

    private fun createViewport(pixelsPerUnit: Float): Viewport {
        val aspectRatio = 16f / 9f
        val viewWidth = 20f * pixelsPerUnit
        val viewport = StretchViewport(viewWidth, viewWidth / aspectRatio, camera)
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
        return viewport
    }

    private fun setupController() {
        controller = LevelController(commandProcessor, textureCache, spriteBatch, shapeRenderer, screenHelper)
        controller.finished.on {
            restart()
        }
    }

    private fun createCommandModule(): CommandModule {
        val executers = listOf(
                RestartExecuter(this),
                DrawDebugExecuter(this))
        return CommandModule(executers)
    }

    // TODO: Put draw debug code in separate class
    private fun createMainTable(): Table {
        val mainTable = uiPack.table()
        mainTable.setFillParent(true)
        fpsLabel = uiPack.label("")
        mainTable.add(fpsLabel).expand().top().right()
        return mainTable
    }

    private fun drawDebug() {
        spriteBatch.projectionMatrix = stage.camera.combined
        spriteBatch.begin()
        val inputCoords = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        val cursorWorldCoords = screenHelper.toWorldCoords(inputCoords)
        val cursorText = "${cursorWorldCoords.x}, ${cursorWorldCoords.y}"
        val cursorDrawCoords = getDrawCoords(inputCoords)
        uiPack.getFont(FontSize.SMALL).draw(spriteBatch, cursorText, cursorDrawCoords.x, cursorDrawCoords.y)
        spriteBatch.end()
    }

    // TODO: There should already be a utility function to perform this calculation, perhaps using viewport.project or something like that
    private fun getDrawCoords(coords: Vector2): Vector2 {
        val screenRatio = Vector2(stage.camera.viewportWidth / Gdx.graphics.width,
                stage.camera.viewportHeight / Gdx.graphics.height)
        val cursorCoords = Vector2(screenRatio.x * coords.x, screenRatio.y * coords.y)
        return Vector2(cursorCoords.x, Gdx.graphics.height * screenRatio.y - cursorCoords.y)
    }

    private inner class LevelInputAdapter : InputAdapter() {
        override fun keyUp(keycode: Int): Boolean {
            when (keycode) {
                Keys.ESCAPE -> {
                    paused.notify(DefaultEvent())
                    return true
                }
            }
            return false
        }
    }
}
