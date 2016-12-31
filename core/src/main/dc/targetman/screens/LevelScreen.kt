package dc.targetman.screens

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.utils.viewport.Viewport
import dc.targetman.level.LevelController
import dclib.system.Input

class LevelScreen(private val controller: LevelController, private val viewport: Viewport) : Screen {
    private val input = Input()

    init {
        input.add(LevelInputAdapter())
    }

    override fun show() {
    }

    override fun render(delta: Float) {
        controller.update(delta)
        controller.draw()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {
    }

    override fun dispose() {
        input.dispose()
        controller.dispose()
    }

    private inner class LevelInputAdapter : InputAdapter() {
        override fun keyUp(keycode: Int): Boolean {
            when (keycode) {
                Keys.ESCAPE -> {
                    controller.toggleRunning()
                    return true
                }
            }
            return false
        }
    }
}
