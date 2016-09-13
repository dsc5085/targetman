package dc.targetman.epf.systems;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import dc.targetman.epf.parts.MovementPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.BodyPart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.systems.EntitySystem;
import dclib.geometry.Transform;

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
			moveLimbsToTransform(entity);
		}
	}

	private void moveLimbsToTransform(final Entity entity) {
		LimbsPart limbsPart = entity.get(LimbsPart.class);
		// TODO: Shouldn't have to call this.  Find an alternative solution
		limbsPart.update();
		Rectangle limbBounds = getBounds(limbsPart.getCollisionTransforms());
		Rectangle transformBounds = entity.get(TransformPart.class).getTransform().getBounds();
		Vector2 global = new Vector2(transformBounds.getCenter(new Vector2()).x, transformBounds.y);
		Transform rootTransform = limbsPart.getRoot().getTransform();
		float localY = limbBounds.y - rootTransform.getPosition().y;
		rootTransform.setGlobal(new Vector2(0, localY), global);
	}

	private final Rectangle getBounds(final List<Transform> transforms) {
		Rectangle bounds = transforms.get(0).getBounds();
		for (Transform transform : transforms) {
			bounds.merge(transform.getBounds());
		}
		return bounds;
	}

}
