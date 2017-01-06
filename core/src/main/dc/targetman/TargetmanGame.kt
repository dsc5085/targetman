package dc.targetman

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.viewport.Viewport
import dc.targetman.level.LevelController
import dc.targetman.screens.LevelScreen
import dclib.graphics.RenderUtils
import dclib.graphics.TextureCache
import dclib.system.ScreenManager

class TargetmanGame : ApplicationAdapter() {
    private val PIXELS_PER_UNIT = 32f

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

    override fun resize(width: Int, height: Int) {
        screenManager.resize(width, height)
    }

	override fun dispose() {
		textureCache.dispose()
		spriteBatch.dispose()
		shapeRenderer.dispose()
	}

	private fun createLevelScreen(): Screen? {
		val viewport = createViewport()
        val camera = viewport.camera as OrthographicCamera
        val controller = LevelController(textureCache, spriteBatch, shapeRenderer, PIXELS_PER_UNIT, camera)
		controller.levelFinished.on { screenManager.swap(createLevelScreen()) }
        return LevelScreen(controller, viewport)
	}

	private fun createTextureCache(): TextureCache {
		val textureCache = TextureCache()
        textureCache.loadTexturesIntoAtlas("textures/objects/", "objects")
        textureCache.loadTexturesIntoAtlas("textures/skins/man", "skins/man")
		return textureCache
	}

    private fun createViewport(): Viewport {
        val aspectRatio = 16f / 9f
		val viewWidth = 20f * PIXELS_PER_UNIT
        val camera = OrthographicCamera()
		val viewport = StretchViewport(viewWidth, viewWidth / aspectRatio, camera)
		viewport.update(Gdx.graphics.width, Gdx.graphics.height)
		return viewport
    }
}