package dc.targetman.gamelogic;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.MovementPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystem;
import dclib.epf.parts.LimbAnimationsPart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.TransformPart;
import dclib.physics.Transform;
import dclib.physics.limb.Limb;
import dclib.physics.limb.LimbAnimation;
import dclib.util.Maths;

public final class MovementSystem extends EntitySystem {

	public MovementSystem(final EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		if (entity.has(MovementPart.class)) {
			move(entity);
			moveLimbsToTransform(entity);
		}
	}

	private void move(final Entity entity) {
		MovementPart movementPart = entity.get(MovementPart.class);
		float direction = movementPart.getDirection();
		LimbAnimation walkAnimation = entity.get(LimbAnimationsPart.class).get("walk");
		if (direction == 0) {
			walkAnimation.stop();
		} else {
			walkAnimation.play();
			entity.get(LimbsPart.class).setFlipX(direction < 0);
			applyMoveForce(entity, movementPart);
		}
	}

	private void applyMoveForce(final Entity entity, final MovementPart movementPart) {
		Transform transform = entity.get(TransformPart.class).getTransform();
		float moveRatio = getMoveRatio(entity);
		float maxSpeedX = movementPart.getMoveSpeed() * moveRatio;
		float velocityX = transform.getVelocity().x;
		float direction = movementPart.getDirection();
		if (Math.signum(velocityX) != direction || Math.abs(velocityX) < maxSpeedX) {
			transform.applyImpulse(new Vector2(direction * moveRatio, 0));
		}
	}

	private float getMoveRatio(final Entity entity) {
		int numMovementLimbs = 0;
		List<Limb> movementLimbs = entity.get(MovementPart.class).getLimbs();
		for (Limb limb : entity.get(LimbsPart.class).getAll()) {
			if (movementLimbs.contains(limb)) {
				numMovementLimbs++;
			}
		}
		return (float)numMovementLimbs / movementLimbs.size();
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
