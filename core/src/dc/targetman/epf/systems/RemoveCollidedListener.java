package dc.targetman.epf.systems;

import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.CollisionRemovePart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.PhysicsPart;
import dclib.physics.BodyType;
import dclib.physics.CollidedListener;

public class RemoveCollidedListener implements CollidedListener {

	private final EntityManager entityManager;

	public RemoveCollidedListener(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public final void collided(final Entity collider, final Entity collidee, final Vector2 offsets) {
		if (collider.hasActive(CollisionRemovePart.class) && collidee.hasActive(PhysicsPart.class)) {
			PhysicsPart collideePhysicsPart = collidee.get(PhysicsPart.class);
			Enum<?>[] collisionGroups = collider.get(CollisionRemovePart.class).getCollisionGroups();
			if (collideePhysicsPart.getBodyType() == BodyType.STATIC
					|| collideePhysicsPart.containsAny(collisionGroups)) {
				entityManager.remove(collider);
			}
		}
	}

}
