package dc.targetman

import com.badlogic.gdx.ApplicationAdapter
import dc.targetman.command.CommandProcessor
import dc.targetman.screens.ConsoleScreen
import dc.targetman.screens.LevelScreen
import dc.targetman.util.Json
import dclib.graphics.Render
import dclib.graphics.RenderUtils
import dclib.graphics.TextureCache
import dclib.system.ScreenManager
import dclib.ui.UiPack

class TargetmanGame : ApplicationAdapter() {
	private val config = Json.toObject<AppConfig>("config.json")
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
		val levelScreen = LevelScreen(config, commandProcessor, textureCache, uiPack, render)
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
		val textureCache = TextureCache()
        textureCache.loadTexturesIntoAtlas("textures/objects", "objects")
        textureCache.loadTexturesIntoAtlas("textures/skins/man", "skins/man")
		return textureCache
	}
}