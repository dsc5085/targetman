package dc.targetman.ai;

import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.AiPart;
import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.epf.util.StickActions;
import dc.targetman.level.models.Alliance;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystem;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.TransformPart;
import dclib.geometry.Centrum;
import dclib.geometry.VectorUtils;

public final class AiSystem extends EntitySystem {

	private final EntityManager entityManager;
	private final StickActions stickActions;

	public AiSystem(final EntityManager entityManager, final StickActions stickActions) {
		super(entityManager);
		this.entityManager = entityManager;
		this.stickActions = stickActions;
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		if (entity.has(AiPart.class)) {
			Vector2 targetPosition = getTargetPosition(entity);
			if (targetPosition != null) {
				move(entity, targetPosition);
				fire(entity, targetPosition);
			}
		}
	}

	private void move(final Entity entity, final Vector2 targetPosition) {
		Vector2 position = entity.get(TransformPart.class).getTransform().getCenter();
		Vector2 targetOffset = VectorUtils.offset(position, targetPosition);
		boolean flipX = entity.get(LimbsPart.class).getFlipX();
		float moveDirection = 0;
		if (targetOffset.x > 0 && flipX) {
			moveDirection = 1;
		} else if (targetOffset.x < 0 && !flipX) {
			moveDirection = -1;
		}
		stickActions.move(entity, moveDirection);
	}

	private void fire(final Entity entity, final Vector2 targetPosition) {
		Centrum centrum = entity.get(WeaponPart.class).getCentrum();
		boolean flipX = entity.get(LimbsPart.class).getFlipX();
		float direction = getRotateDirection(centrum, targetPosition, flipX);
		stickActions.aim(entity, direction);
		stickActions.trigger(entity);
	}

	private Vector2 getTargetPosition(final Entity entity) {
		for (Entity target : entityManager.getAll()) {
			if (target.is(Alliance.PLAYER)) {
				return target.get(TransformPart.class).getTransform().getCenter();
			}
		}
		return null;
	}

	/**
	 * Returns float indicating how rotation should change.
	 * @param from from
	 * @param to to
	 * @param currentAngle currentAngle
	 * @param flipX flipX
	 * @return 1 if angle should be increased, -1 if angle should be decreased, or 0 if angle shouldn't change
	 */
	private float getRotateDirection(final Centrum fromCentrum, final Vector2 to, final boolean flipX) {
		float direction = 0;
		Vector2 offset = VectorUtils.offset(fromCentrum.getPosition(), to);
		Vector2 fireDirection = new Vector2(1, 0).setAngle(fromCentrum.getRotation());
		if (offset.y * fireDirection.x > offset.x * fireDirection.y) {
			direction = 1;
		} else {
			direction = -1;
		}
		if (flipX) {
			direction *= -1;
		}
		return direction;
	}

}
