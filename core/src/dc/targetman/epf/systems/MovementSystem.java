package dc.targetman.epf.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import dc.targetman.epf.parts.BodyPart;
import dc.targetman.epf.parts.MovementPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.systems.EntitySystem;

public final class MovementSystem extends EntitySystem {

	public MovementSystem(final EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		MovementPart movementPart = entity.tryGet(MovementPart.class);
		if (movementPart != null) {
			Body body = entity.get(BodyPart.class).getBody();
			Vector2 velocity = body.getLinearVelocity();
			float maxSpeed = movementPart.getMoveSpeed();
			if (Math.abs(velocity.x) > maxSpeed) {
				velocity.x = Math.signum(velocity.x) * maxSpeed;
				body.setLinearVelocity(velocity);
			}
		}
	}

}
