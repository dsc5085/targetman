package dc.targetman.screens;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;

import dc.targetman.level.LevelController;
import dclib.system.Input;

public final class LevelScreen implements Screen {

	private final Input input = new Input();
	private final LevelController controller;

	public LevelScreen(final LevelController controller) {
		this.controller = controller;
		input.add(new LevelInputAdapter());
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
		input.dispose();
		controller.dispose();
	}

	private final class LevelInputAdapter extends InputAdapter {

		@Override
		public final boolean keyUp(final int keycode) {
			switch (keycode) {
			case Keys.ESCAPE:
				controller.toggleRunning();
				return true;
			};
			return false;
		}

	}

}
