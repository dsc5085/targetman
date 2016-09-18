package dc.targetman.gamelogic;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.MovementPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystem;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.TransformPart;
import dclib.physics.Transform;
import dclib.physics.limb.Limb;
import dclib.util.Maths;

public final class MovementSystem extends EntitySystem {

	public MovementSystem(final EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		MovementPart movementPart = entity.tryGet(MovementPart.class);
		if (movementPart != null) {
			Transform transform = entity.get(TransformPart.class).getTransform();
			Vector2 velocity = transform.getVelocity();
			float maxSpeed = movementPart.getMoveSpeed();
			if (Math.abs(velocity.x) > maxSpeed) {
				velocity.x = Math.signum(velocity.x) * maxSpeed;
				transform.setVelocity(velocity);
			}
			moveLimbsToTransform(entity);
		}
	}

	private void moveLimbsToTransform(final Entity entity) {
		LimbsPart limbsPart = entity.get(LimbsPart.class);
		// TODO: Shouldn't have to call this.  Find an alternative solution, such as making the box2d transform a root
		limbsPart.update();
		Rectangle transformBounds = entity.get(TransformPart.class).getTransform().getBounds();
		Vector2 global = new Vector2(transformBounds.getCenter(new Vector2()).x, transformBounds.y);
		Transform rootTransform = limbsPart.getRoot().getTransform();
		float localY = getY(limbsPart) - rootTransform.getPosition().y;
		rootTransform.setGlobal(new Vector2(0, localY), global);
	}

	private final float getY(final LimbsPart limbsPart) {
		float y = Float.NaN;
		for (Limb descendant : limbsPart.getRoot().getDescendants()) {
			float newY = descendant.getTransform().getBounds().y;
			y = Maths.min(y, newY);
		}
		return y;
	}

}
