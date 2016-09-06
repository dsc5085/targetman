package dc.targetman.epf.systems;

import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.ScalePart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.TransformPart;
import dclib.epf.systems.EntitySystem;
import dclib.geometry.Transform;
import dclib.util.Timer;

public final class ScaleSystem extends EntitySystem {

	public ScaleSystem(final EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		ScalePart scalePart = entity.tryGet(ScalePart.class);
		if (scalePart != null) {
			Timer scaleTimer = scalePart.getScaleTimer();
			scaleTimer.tick(delta);
			float scaleX = scalePart.getScaleX();
			Transform transform = entity.get(TransformPart.class).getTransform();
			transform.setScale(new Vector2(scaleX, transform.getScale().y));
		}
	}

}
