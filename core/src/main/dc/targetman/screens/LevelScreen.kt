package dc.targetman.screens

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.utils.viewport.Viewport
import dc.targetman.level.LevelController
import dclib.eventing.DefaultEvent
import dclib.eventing.EventDelegate
import dclib.system.Input

class LevelScreen(private val controller: LevelController, private val viewport: Viewport) : Screen {
    val paused = EventDelegate<DefaultEvent>()

    private val input = Input()

    init {
        input.add(LevelInputAdapter())
    }

    override fun show() {
        input.enable()
    }

    override fun hide() {
        input.disable()
    }

    override fun render(delta: Float) {
        controller.draw()
        controller.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
        controller.dispose()
    }

    private inner class LevelInputAdapter : InputAdapter() {
        override fun keyUp(keycode: Int): Boolean {
            when (keycode) {
                Keys.ESCAPE -> {
                    // TODO: Implement pause/resume instead
                    paused.notify(DefaultEvent())
                    return true
                }
            }
            return false
        }
    }
}
