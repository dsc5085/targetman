package dc.targetman

import com.badlogic.gdx.ApplicationAdapter
import dc.targetman.command.CommandProcessor
import dc.targetman.geometry.PolygonOperations
import dc.targetman.screens.ConsoleScreen
import dc.targetman.screens.LevelScreen
import dclib.geometry.PolygonUtils
import dclib.graphics.Render
import dclib.graphics.RenderUtils
import dclib.graphics.TextureCache
import dclib.graphics.TextureUtils
import dclib.system.ScreenManager
import dclib.ui.UiPack

class TargetmanGame : ApplicationAdapter() {
	private val commandProcessor = CommandProcessor()
	private val screenManager = ScreenManager()
	private lateinit var textureCache: TextureCache
	private lateinit var render: Render
	private lateinit var uiPack: UiPack
	
	override fun create() {
		textureCache = createTextureCache()
		render = Render()
		uiPack = UiPack("ui/test/uiskin.json", "ui/ocr/ocr_32.fnt", "ui/ocr/ocr_24.fnt")
		val consoleScreen = ConsoleScreen(commandProcessor, uiPack)
		val levelScreen = LevelScreen(commandProcessor, textureCache, uiPack, render)
		link(consoleScreen, levelScreen)
		screenManager.add(levelScreen)
        screenManager.add(consoleScreen, false)
	}

	private fun link(consoleScreen: ConsoleScreen, levelScreen: LevelScreen) {
		consoleScreen.closed.on {
			consoleScreen.hide()
			levelScreen.show()
		}
		levelScreen.paused.on {
			levelScreen.pause()
			consoleScreen.show()
		}
	}

	override fun render() {
		RenderUtils.clear()
		screenManager.render()
	}

    override fun resize(width: Int, height: Int) {
        screenManager.resize(width, height)
    }

	override fun dispose() {
		uiPack.dispose()
		textureCache.dispose()
		render.dispose()
	}

	private fun createTextureCache(): TextureCache {
		val textureCache = TextureCache({
			val distanceToleranceRatio = 0.1
			val vertices = TextureUtils.createConvexHull(it)
			val vectors = PolygonUtils.toVectors(vertices)
			val distanceTolerance = Math.max(it.regionWidth, it.regionHeight) * distanceToleranceRatio
			PolygonUtils.toFloats(PolygonOperations.simplify(vectors, distanceTolerance))
		})
        textureCache.loadTexturesIntoAtlas("textures/objects/", "objects")
        textureCache.loadTexturesIntoAtlas("textures/skins/man", "skins/man")
		return textureCache
	}
}