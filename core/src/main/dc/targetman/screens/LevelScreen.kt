package dc.targetman.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.StretchViewport
import dc.targetman.command.CommandModule
import dc.targetman.command.CommandProcessor
import dc.targetman.level.DebugView
import dc.targetman.level.LevelController
import dc.targetman.level.executers.DrawDebugExecuter
import dc.targetman.level.executers.RestartExecuter
import dclib.eventing.DefaultEvent
import dclib.eventing.EventDelegate
import dclib.graphics.Render
import dclib.graphics.ScreenHelper
import dclib.graphics.TextureCache
import dclib.system.Screen
import dclib.ui.UiPack

class LevelScreen(
        private val commandProcessor: CommandProcessor,
        private val textureCache: TextureCache,
        uiPack: UiPack,
        private val render: Render
) : Screen() {
    val paused = EventDelegate<DefaultEvent>()

    private val camera = OrthographicCamera()
    private val screenHelper = createScreenHelper(render)
    private lateinit var controller: LevelController
    private val commandModule: CommandModule
    private val debugView = DebugView(uiPack, render.sprite, screenHelper, stage)

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
        debugView.update()
    }

    override fun draw() {
        controller.draw()
        debugView.draw()
    }

    override fun resize(width: Int, height: Int) {
        screenHelper.viewport.update(width, height)
    }

    override fun dispose() {
        commandModule.dispose()
        controller.dispose()
    }

    private fun createScreenHelper(render: Render): ScreenHelper {
        val aspectRatio = 16f / 9f
        val viewWidth = 20f * render.pixelsPerUnit
        val viewport = StretchViewport(viewWidth, viewWidth / aspectRatio, camera)
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
        return ScreenHelper(render.pixelsPerUnit, viewport)
    }

    private fun setupController() {
        controller = LevelController(commandProcessor, textureCache, render, screenHelper)
        controller.finished.on {
            restart()
        }
    }

    private fun createCommandModule(): CommandModule {
        val executers = listOf(
                RestartExecuter(this),
                DrawDebugExecuter(debugView))
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
