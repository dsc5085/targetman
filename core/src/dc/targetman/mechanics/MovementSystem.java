package dc.targetman.mechanics;

import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import dc.targetman.epf.parts.MovementPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystem;
import dclib.epf.parts.LimbAnimationsPart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.TransformPart;
import dclib.physics.Box2dUtils;
import dclib.physics.Transform;
import dclib.physics.limb.Limb;
import dclib.physics.limb.LimbAnimation;
import dclib.util.Maths;

public final class MovementSystem extends EntitySystem {

	private final World world;

	public MovementSystem(final EntityManager entityManager, final World world) {
		super(entityManager);
		this.world = world;
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		if (entity.has(MovementPart.class)) {
			move(entity);
			jump(entity);
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
			movementPart.setDirection(0);
		}
	}

	private void applyMoveForce(final Entity entity, final MovementPart movementPart) {
		Transform transform = entity.get(TransformPart.class).getTransform();
		float maxSpeedX = movementPart.getMoveSpeed() * getMoveStrength(entity);
		float velocityX = transform.getVelocity().x;
		float direction = movementPart.getDirection();
		if (Math.signum(velocityX) != direction || Math.abs(velocityX) < maxSpeedX) {
			transform.applyImpulse(new Vector2(direction, 0));
		}
	}

	private final void jump(final Entity entity) {
		MovementPart movementPart = entity.get(MovementPart.class);
		if (movementPart.jumping()) {
			Transform transform = entity.get(TransformPart.class).getTransform();
			Body body = Box2dUtils.findBody(world, entity);
			if (isGrounded(body)) {
				Vector2 position = transform.getPosition();
				transform.setVelocity(new Vector2(transform.getVelocity().x, 0));
				transform.setPosition(new Vector2(position.x, position.y + MathUtils.FLOAT_ROUNDING_ERROR));
				float jumpForce = entity.get(MovementPart.class).getJumpForce() * getMoveStrength(entity);;
				transform.applyImpulse(new Vector2(0, jumpForce));
			}
		}
		movementPart.setJumping(false);
	}

	private boolean isGrounded(final Body body) {
		if (body.getLinearVelocity().y == 0) {
			for (Contact contact : world.getContactList()) {
				if (isGroundedContact(body, contact)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isGroundedContact(final Body body, final Contact contact) {
		// TODO: Don't use get(0) because its hardcoded
		Fixture legsFixture = body.getFixtureList().get(0);
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		return contact.isTouching() && (legsFixture == fixtureA && !fixtureB.isSensor())
				|| (legsFixture == fixtureB && !fixtureA.isSensor());
	}

	private float getMoveStrength(final Entity entity) {
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
		Transform rootTransform = limbsPart.getRoot().getTransform();
		float localY = getY(limbsPart) - rootTransform.getPosition().y;
		Vector2 global = EntityUtils.getBase(entity);
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
