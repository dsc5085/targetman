package dc.targetman.epf.graphics;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dc.targetman.ai.DefaultNode;
import dc.targetman.epf.parts.AiPart;
import dclib.epf.Entity;
import dclib.epf.graphics.EntityDrawer;
import dclib.epf.parts.TransformPart;
import dclib.geometry.RectangleUtils;

public final class EntityGraphDrawer implements EntityDrawer {

	private final ShapeRenderer shapeRenderer;
	private final Camera camera;
	private final float pixelsPerUnit;

	public EntityGraphDrawer(final ShapeRenderer shapeRenderer, final Camera camera, final float pixelsPerUnit) {
		this.shapeRenderer = shapeRenderer;
		this.camera = camera;
		this.pixelsPerUnit = pixelsPerUnit;
	}

	@Override
	public final void draw(final List<Entity> entities) {
		Matrix4 renderMatrix = new Matrix4(camera.combined);
		renderMatrix.scale(pixelsPerUnit, pixelsPerUnit, 1);
		shapeRenderer.setProjectionMatrix(renderMatrix);
		shapeRenderer.setColor(Color.CYAN);
		shapeRenderer.begin(ShapeType.Line);
		for (Entity entity : entities) {
			draw(entity);
		}
		shapeRenderer.end();
	}

	private void draw(final Entity entity) {
		AiPart aiPart = entity.tryGet(AiPart.class);
		if (aiPart != null && !aiPart.getPath().isEmpty()) {
			List<Vector2> path = toVectors(aiPart.getPath());
			Rectangle bounds = entity.get(TransformPart.class).getTransform().getBounds();
			Vector2 start = RectangleUtils.base(bounds);
			Vector2 end = path.get(0);
			for (int i = 1; i < path.size(); i++) {
				shapeRenderer.line(start, end);
				start = path.get(i - 1);
				end = path.get(i);
			}
		}
	}

	private List<Vector2> toVectors(final List<DefaultNode> path) {
		List<Vector2> vectors = new ArrayList<Vector2>();
		for (DefaultNode node : path) {
			vectors.add(node.getPosition());
		}
		return vectors;
	}

}
