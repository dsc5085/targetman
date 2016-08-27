package dc.targetman.epf.systems;

import com.badlogic.gdx.math.Polygon;

import dc.targetman.epf.parts.ScalePart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.TransformPart;
import dclib.epf.systems.EntitySystem;
import dclib.util.Timer;

public final class ScaleSystem extends EntitySystem {

	public ScaleSystem(final EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	public final void update(final float delta, final Entity entity) {
		if (entity.hasActive(ScalePart.class)) {
			ScalePart scalePart = entity.get(ScalePart.class);
			Timer scaleTimer = scalePart.getScaleTimer();
			scaleTimer.tick(delta);
			float scaleX = scalePart.getScaleX();
			Polygon polygon = entity.get(TransformPart.class).getPolygon();
			polygon.setScale(scaleX, polygon.getScaleY());
		}
	}

}
