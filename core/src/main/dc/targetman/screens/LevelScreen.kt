package dc.targetman.screens

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.utils.viewport.Viewport
import dc.targetman.level.LevelController
import dclib.eventing.DefaultEvent
import dclib.eventing.EventDelegate
import dclib.system.Screen

class LevelScreen(private val controller: LevelController, private val viewport: Viewport) : Screen() {
    val paused = EventDelegate<DefaultEvent>()

    init {
        add(LevelInputAdapter())
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
