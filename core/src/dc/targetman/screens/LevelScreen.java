package dc.targetman.screens;

import com.badlogic.gdx.Screen;

import dc.targetman.level.LevelController;

public final class LevelScreen implements Screen {

	private final LevelController controller;

	public LevelScreen(final LevelController controller) {
		this.controller = controller;
	}

	@Override
	public void show() {
	}

	@Override
	public void render(final float delta) {
		controller.update(delta);
		controller.draw();
	}

	@Override
	public void resize(final int width, final int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
		controller.dispose();
	}

}
