package dc.targetman.system

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.BufferUtils
import dclib.graphics.ScreenHelper
import org.lwjgl.LWJGLException
import org.lwjgl.input.Cursor
import org.lwjgl.input.Mouse

object InputUtils {
    private var hiddenCursor: Cursor? = null

    fun getCursorWorldCoord(screenHelper: ScreenHelper): Vector2 {
        val inputCoords = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        return screenHelper.toWorldCoord(inputCoords)
    }

    fun setCursorVisible(isVisible: Boolean) {
        if (Gdx.app.type != Application.ApplicationType.Desktop && Gdx.app is LwjglApplication) {
            return
        }
        try {
            if (hiddenCursor == null) {
                if (Mouse.isCreated()) {
                    val minCursorSize = Cursor.getMinCursorSize()
                    val images = BufferUtils.newIntBuffer(minCursorSize * minCursorSize)
                    hiddenCursor = Cursor(minCursorSize, minCursorSize, minCursorSize / 2, minCursorSize / 2,
                            1, images, null)
                } else {
                    throw LWJGLException("Could not create empty cursor before Mouse is created")
                }
            }
            Mouse.setNativeCursor(if (isVisible) null else hiddenCursor)
        } catch (e: LWJGLException) {
            throw RuntimeException(e)
        }
    }
}