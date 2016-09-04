package dc.targetman.epf.systems;

import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.MovementPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.TransformPart;
import dclib.epf.systems.EntitySystem;

public final class MovementSystem extends EntitySystem {

	public MovementSystem(final EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		MovementPart movementPart = entity.tryGet(MovementPart.class);
		if (movementPart != null) {
			TransformPart transformPart = entity.get(TransformPart.class);
			Vector2 offset = movementPart.getVelocity().scl(delta);
			transformPart.translate(offset);
		}
	}

}
