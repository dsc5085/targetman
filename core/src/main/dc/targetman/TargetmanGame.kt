package dc.targetman

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import dc.targetman.level.LevelController
import dc.targetman.screens.LevelScreen
import dclib.graphics.RenderUtils
import dclib.graphics.TextureCache
import dclib.system.ScreenManager

class TargetmanGame : ApplicationAdapter() {
	private val screenManager = ScreenManager()
	private lateinit var textureCache: TextureCache
	private lateinit var spriteBatch: PolygonSpriteBatch
	private lateinit var shapeRenderer: ShapeRenderer
	
	override fun create() {
		textureCache = createTextureCache()
		spriteBatch = PolygonSpriteBatch()
		shapeRenderer = ShapeRenderer()
		screenManager.add(createLevelScreen())
	}

	override fun render() {
		RenderUtils.clear()
		screenManager.render()
	}

	override fun dispose() {
		textureCache.dispose()
		spriteBatch.dispose()
		shapeRenderer.dispose()
	}

	private fun createLevelScreen(): Screen? {
		val controller = LevelController(textureCache, spriteBatch, shapeRenderer)
		return LevelScreen(controller)
	}

	private fun createTextureCache(): TextureCache {
		val textureCache = TextureCache()
        textureCache.loadTexturesIntoAtlas("textures/objects/", "objects")
        textureCache.loadTexturesIntoAtlas("textures/skins/man", "skins/man")
		return textureCache
	}
}