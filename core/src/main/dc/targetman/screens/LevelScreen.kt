package dc.targetman.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.viewport.Viewport
import dc.targetman.command.CommandModule
import dc.targetman.command.CommandProcessor
import dc.targetman.level.LevelController
import dc.targetman.level.executers.RestartExecuter
import dclib.eventing.DefaultEvent
import dclib.eventing.EventDelegate
import dclib.graphics.TextureCache
import dclib.system.Screen

class LevelScreen(
        private val commandProcessor: CommandProcessor,
        private val textureCache: TextureCache,
        private val spriteBatch: PolygonSpriteBatch,
        private val shapeRenderer: ShapeRenderer,
        private val pixelsPerUnit: Float
) : Screen() {
    val paused = EventDelegate<DefaultEvent>()

    private val viewport = createViewport(pixelsPerUnit)
    private lateinit var controller: LevelController
    private val commandModule: CommandModule

    init {
        setupController()
        add(LevelInputAdapter())
        commandModule = createCommandModule()
        commandProcessor.add(commandModule)
    }

    fun restart() {
        controller.dispose()
        setupController()
    }

    override fun update(delta: Float) {
        controller.update(delta)
    }

    override fun draw() {
        controller.draw()
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
        val camera = OrthographicCamera()
        val viewport = StretchViewport(viewWidth, viewWidth / aspectRatio, camera)
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
        return viewport
    }

    private fun setupController() {
        controller = LevelController(commandProcessor, textureCache, spriteBatch, shapeRenderer, pixelsPerUnit,
                viewport.camera as OrthographicCamera)
        controller.finished.on {
            restart()
        }
    }

    private fun createCommandModule(): CommandModule {
        val executers = listOf(RestartExecuter(this))
        return CommandModule(executers)
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
