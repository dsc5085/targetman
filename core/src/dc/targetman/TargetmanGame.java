package dc.targetman;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import dc.targetman.level.LevelController;
import dc.targetman.screens.LevelScreen;
import dclib.graphics.ScreenUtils;
import dclib.graphics.TextureCache;
import dclib.system.ScreenManager;

public class TargetmanGame extends ApplicationAdapter {

	private final ScreenManager screenManager = new ScreenManager();
	private TextureCache textureCache;
	private PolygonSpriteBatch spriteBatch;
	private ShapeRenderer shapeRenderer;

	@Override
	public void create () {
		textureCache = createTextureCache();
		spriteBatch = new PolygonSpriteBatch();
		shapeRenderer = new ShapeRenderer();
		screenManager.add(createLevelScreen());
	}

	@Override
	public void render () {
		ScreenUtils.clear();
		screenManager.render();
	}

	@Override
	public void dispose () {
		textureCache.dispose();
		spriteBatch.dispose();
		shapeRenderer.dispose();
	}

	private Screen createLevelScreen() {
		LevelController controller = new LevelController(textureCache, spriteBatch, shapeRenderer);
		return new LevelScreen(controller);
	}

	private TextureCache createTextureCache() {
		TextureCache textureCache = new TextureCache();
		textureCache.addTexturesAsAtlas("textures/objects/", "objects");
		return textureCache;
	}

}
