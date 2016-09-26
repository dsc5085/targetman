package dc.targetman.ai;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.AiPart;
import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.gamelogic.EntityUtils;
import dc.targetman.gamelogic.StickActions;
import dc.targetman.level.DefaultNode;
import dc.targetman.level.models.Alliance;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystem;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.TransformPart;
import dclib.geometry.Centrum;
import dclib.geometry.RectangleUtils;
import dclib.geometry.VectorUtils;

public final class AiSystem extends EntitySystem {

	private final EntityManager entityManager;
	private final PathCreator pathCreator;

	// TODO: Don't update every frame
	public AiSystem(final EntityManager entityManager, final PathCreator pathCreator) {
		super(entityManager);
		this.entityManager = entityManager;
		this.pathCreator = pathCreator;
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		if (entity.has(AiPart.class)) {
			Entity target = getTarget(entity);
			if (target != null) {
				move(entity, target);
				fire(entity, target);
			}
		}
	}

	private Entity getTarget(final Entity entity) {
		for (Entity target : entityManager.getAll()) {
			if (target.is(Alliance.PLAYER) && target.has(LimbsPart.class)) {
				return target;
			}
		}
		return null;
	}

	private void move(final Entity entity, final Entity target) {
		Vector2 base = EntityUtils.getBase(entity);
		Vector2 targetBase = EntityUtils.getBase(target);
		GraphPath<DefaultNode> path = pathCreator.createPath(base, targetBase);
		DefaultNode currentNode = null;
		for (int i = 0; i < path.getCount(); i++) {
			DefaultNode node = path.get(i);
			if (node.at(base)) {
				currentNode = node;
			} else {
				int moveDirection = node.x() > base.x ? 1 : -1;
				StickActions.move(entity, moveDirection);
				jump(entity, currentNode, moveDirection);
				break;
			}
		}
	}

	private void jump(final Entity entity, final DefaultNode currentNode, final int moveDirection) {
		final float edgeThreshold = 0.1f;
		if (currentNode != null) {
			Rectangle bounds = entity.get(TransformPart.class).getTransform().getBounds();
			float edgeX = moveDirection > 0 ? currentNode.right() : currentNode.x();
			if (edgeX >= bounds.x - edgeThreshold && edgeX <= RectangleUtils.right(bounds) + edgeThreshold) {
				StickActions.jump(entity);
			}
		}
	}

	private void fire(final Entity entity, final Entity target) {
		Centrum centrum = entity.get(WeaponPart.class).getCentrum();
		boolean flipX = entity.get(LimbsPart.class).getFlipX();
		Vector2 targetCenter = target.get(TransformPart.class).getTransform().getCenter();
		float direction = getRotateDirection(centrum, targetCenter, flipX);
		StickActions.aim(entity, direction);
		StickActions.trigger(entity);
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
