package dc.targetman.mechanics;

import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;

import dc.targetman.epf.parts.MovementPart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystem;
import dclib.epf.parts.LimbAnimationsPart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.TransformPart;
import dclib.geometry.RectangleUtils;
import dclib.physics.Box2dUtils;
import dclib.physics.Transform;
import dclib.physics.limb.Limb;
import dclib.physics.limb.LimbAnimation;
import dclib.util.Maths;
import net.dermetfan.gdx.physics.box2d.Box2DUtils;

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
		final float minSpeedToAdjust = 0.5f;
		MovementPart movementPart = entity.get(MovementPart.class);
		Direction direction = movementPart.getDirection();
		LimbAnimation walkAnimation = entity.get(LimbAnimationsPart.class).get("walk");
		Direction forceDirection = direction;
		if (direction == Direction.NONE) {
			walkAnimation.stop();
			Vector2 velocity = entity.get(TransformPart.class).getTransform().getVelocity();
			if (Math.abs(velocity.x) > minSpeedToAdjust) {
				forceDirection = Direction.from(-velocity.x);
			}
		} else {
			walkAnimation.play();
			entity.get(LimbsPart.class).setFlipX(direction == Direction.LEFT);
		}
		applyMoveForce(entity, movementPart.getMoveSpeed(), forceDirection);
	}

	private void applyMoveForce(final Entity entity, final float speed, final Direction direction) {
		Transform transform = entity.get(TransformPart.class).getTransform();
		float maxSpeedX = speed * getMoveStrength(entity);
		float velocityX = transform.getVelocity().x;
		if (Direction.from(velocityX) != direction || Math.abs(velocityX) < maxSpeedX) {
			transform.applyImpulse(new Vector2(direction.toFloat(), 0));
		}
	}

	private final void jump(final Entity entity) {
		MovementPart movementPart = entity.get(MovementPart.class);
		if (movementPart.jumping()) {
			Transform transform = entity.get(TransformPart.class).getTransform();
			Body body = Box2dUtils.INSTANCE.getBody(entity);
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
				Fixture fixtureA = contact.getFixtureA();
				Fixture fixtureB = contact.getFixtureB();
				if (contact.isTouching() && (isGroundedContact(body, fixtureA, fixtureB, contact)
						|| isGroundedContact(body, fixtureB, fixtureA, contact))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isGroundedContact(final Body body, final Fixture fixture1, final Fixture fixture2,
			final Contact contact) {
		// TODO: Don't use get(0) because its hardcoded
		Fixture legsFixture = body.getFixtureList().get(0);
		WorldManifold manifold = contact.getWorldManifold();
		float halfLegsSize = Box2DUtils.height(legsFixture) / 2;
		if (legsFixture == fixture1 && !fixture2.isSensor()) {
			float legsMinY = Box2DUtils.minYWorld(legsFixture);
			for (int i = 0; i < manifold.getNumberOfContactPoints(); i++) {
				float maxYForGrounded = legsMinY + halfLegsSize;
				if (manifold.getPoints()[i].y < maxYForGrounded) {
					return true;
				}
			}
		}
		return false;
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
		Rectangle bounds = entity.get(TransformPart.class).getTransform().getBounds();
		Vector2 global = RectangleUtils.base(bounds);
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
