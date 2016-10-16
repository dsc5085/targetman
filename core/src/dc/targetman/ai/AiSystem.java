package dc.targetman.ai;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.AiPart;
import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.mechanics.Alliance;
import dc.targetman.mechanics.StickActions;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystem;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.TransformPart;
import dclib.geometry.Centrum;
import dclib.geometry.VectorUtils;

public final class AiSystem extends EntitySystem {

	private final EntityManager entityManager;
	private final GraphHelper graphHelper;

	public AiSystem(final EntityManager entityManager, final GraphHelper graphHelper) {
		super(entityManager);
		this.entityManager = entityManager;
		this.graphHelper = graphHelper;
	}

	@Override
	protected final void update(final float delta, final Entity entity) {
		AiPart aiPart = entity.tryGet(AiPart.class);
		if (aiPart != null) {
			aiPart.tick(delta);
			Entity target = findTarget(entity);
			if (target != null) {
				boolean thinking = entity.get(AiPart.class).think();
				Ai ai = new Ai(entity, thinking);
				Rectangle targetBounds = target.get(TransformPart.class).getTransform().getBounds();
				navigate(ai, targetBounds);
				aim(entity, targetBounds);
			}
		}
	}

	private Entity findTarget(final Entity entity) {
		for (Entity target : entityManager.getAll()) {
			if (target.is(Alliance.PLAYER) && target.has(LimbsPart.class)) {
				return target;
			}
		}
		return null;
	}

	private void navigate(final Ai ai, final Rectangle targetBounds) {
		move(ai, targetBounds);
		jump(ai);
		updatePath(ai, targetBounds);
	}

	private void move(final Ai ai, final Rectangle targetBounds) {
		int moveDirection = 0;
		float nextX = getNextX(ai, targetBounds);
		if (!Float.isNaN(nextX)) {
			float offsetX = nextX - ai.bounds.getCenter(new Vector2()).x;
			if (Math.abs(offsetX) > ai.bounds.width / 2) {
				moveDirection = offsetX > 0 ? 1 : -1;
			}
		}
		StickActions.move(ai.entity, moveDirection);
	}

	private float getNextX(final Ai ai, final Rectangle targetBounds) {
		float nextX = Float.NaN;
		Segment targetSegment = graphHelper.getNearestSegment(targetBounds);
		if (targetSegment != null && targetSegment.nodes.contains(ai.currentNode)) {
			nextX = targetBounds.getCenter(new Vector2()).x;
		} else if (ai.nextNode != null) {
			nextX = ai.nextNode.x();
		}
		return nextX;
	}

	private void jump(final Ai ai) {
		if (ai.currentNode != null) {
			StickActions.jump(ai.entity);
		}
	}

	private void updatePath(final Ai ai, final Rectangle targetBounds) {
		if (ai.thinking && ai.currentNode != null) {
			Segment targetSegment = graphHelper.getNearestSegment(targetBounds);
			List<DefaultNode> newPath = graphHelper.createPath(ai.currentNode, targetSegment.leftNode);
			ai.setPath(newPath);
		}
	}

	private void aim(final Entity entity, final Rectangle targetBounds) {
		Centrum centrum = entity.get(WeaponPart.class).getCentrum();
		boolean flipX = entity.get(LimbsPart.class).getFlipX();
		Vector2 targetCenter = targetBounds.getCenter(new Vector2());
		float direction = getRotateDirection(centrum, targetCenter, flipX);
		StickActions.aim(entity, direction);
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
		Vector2 offset = VectorUtils.offset(fromCentrum.getPosition(), to);
		Vector2 fireDirection = new Vector2(1, 0).setAngle(fromCentrum.getRotation());
		float direction = offset.y * fireDirection.x > offset.x * fireDirection.y ? 1 : -1;
		if (flipX) {
			direction *= -1;
		}
		return direction;
	}

	private class Ai {

		public final Entity entity;
		public final boolean thinking;
		public final Rectangle bounds;
		public final List<DefaultNode> path;
		// TODO: currentNode logic not right
		public final DefaultNode currentNode;
		public final DefaultNode nextNode;

		public Ai(final Entity entity, final boolean thinking) {
			this.entity = entity;
			this.thinking = thinking;
			bounds = entity.get(TransformPart.class).getTransform().getBounds();
			currentNode = graphHelper.getTouchingNode(bounds);
			path = entity.get(AiPart.class).getPath();
			path.remove(currentNode);
			nextNode = path.isEmpty() ? null : path.get(0);
		}

		public final void setPath(final List<DefaultNode> path) {
			entity.get(AiPart.class).setPath(path);
		}

	}

}
