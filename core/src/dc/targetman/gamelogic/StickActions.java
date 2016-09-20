package dc.targetman.gamelogic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.utils.Array;

import dc.targetman.epf.parts.MovementPart;
import dc.targetman.epf.parts.WeaponPart;
import dclib.epf.Entity;
import dclib.epf.parts.TransformPart;
import dclib.physics.Box2dUtils;
import dclib.physics.Transform;
import net.dermetfan.gdx.physics.box2d.Box2DUtils;

public final class StickActions {

	private final World world;

	public StickActions(final World world) {
		this.world = world;
	}

	public final void move(final Entity entity, final float direction) {
		entity.get(MovementPart.class).setDirection(direction);
	}

	public final void jump(final Entity entity) {
		Transform transform = entity.get(TransformPart.class).getTransform();
		Body body = Box2dUtils.findBody(world, entity);
		if (isGrounded(body)) {
			Vector2 position = transform.getPosition();
			transform.setVelocity(new Vector2(transform.getVelocity().x, 0));
			transform.setPosition(new Vector2(position.x, position.y + MathUtils.FLOAT_ROUNDING_ERROR));
			float jumpSpeed = entity.get(MovementPart.class).getJumpSpeed();
			transform.applyImpulse(new Vector2(0, jumpSpeed));
		}
	}

	public final void aim(final Entity entity, final float direction) {
		entity.get(WeaponPart.class).setAimDirection(direction);
	}

	public final void trigger(final Entity entity) {
		entity.get(WeaponPart.class).setTriggered(true);
	}

	private boolean isGrounded(final Body body) {
		// TODO: Figure out correct value to use.  This is just a guess
		float halfHeight = Box2DUtils.height(body) / 2;
		for (Contact contact : world.getContactList()) {
			if (contact.isTouching() && isGroundedContact(body, contact)) {
				Vector2 position = body.getPosition();
				WorldManifold manifold = contact.getWorldManifold();
				for (int i = 0; i < manifold.getNumberOfContactPoints(); i++) {
					if (manifold.getPoints()[i].y >= position.y - halfHeight) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	private boolean isGroundedContact(final Body body, final Contact contact) {
		Array<Fixture> fixtures = body.getFixtureList();
		if (fixtures.contains(contact.getFixtureA(), true)) {
			return !contact.getFixtureB().isSensor();
		} else if (fixtures.contains(contact.getFixtureB(), true)) {
			return !contact.getFixtureA().isSensor();
		}
		return false;
	}

}
