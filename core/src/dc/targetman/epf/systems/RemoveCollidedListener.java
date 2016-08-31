package dc.targetman.epf.systems;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.CollisionRemovePart;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.parts.PhysicsPart;
import dclib.physics.BodyType;
import dclib.physics.CollidedListener;

public final class RemoveCollidedListener implements CollidedListener {

	private final EntityManager entityManager;

	public RemoveCollidedListener(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public final void collided(final Entity collider, final Entity collidee, final Vector2 offsets) {
		CollisionRemovePart collisionRemovePart = collider.tryGet(CollisionRemovePart.class);
		PhysicsPart collideePhysicsPart = collidee.tryGet(PhysicsPart.class);
		if (collisionRemovePart != null && collideePhysicsPart != null) {
			List<Enum<?>> collisionGroups = collisionRemovePart.getCollisionGroups();
			if (collideePhysicsPart.getBodyType() == BodyType.STATIC
					|| collideePhysicsPart.containsAny(collisionGroups)) {
				entityManager.remove(collider);
			}
		}
	}

}
