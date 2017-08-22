package dc.targetman.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import dclib.graphics.ScreenHelper

object InputUtils {
    fun getCursorWorldCoord(screenHelper: ScreenHelper): Vector2 {
        val inputCoords = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        return screenHelper.toWorldCoord(inputCoords)
    }
}