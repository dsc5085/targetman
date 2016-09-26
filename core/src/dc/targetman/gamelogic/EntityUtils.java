package dc.targetman.gamelogic;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dclib.epf.Entity;
import dclib.epf.parts.TransformPart;

public final class EntityUtils {

	private EntityUtils() {
	}

	public static Vector2 getBase(final Entity entity) {
		Rectangle bounds = entity.get(TransformPart.class).getTransform().getBounds();
		return new Vector2(bounds.getCenter(new Vector2()).x, bounds.y);
	}

}
