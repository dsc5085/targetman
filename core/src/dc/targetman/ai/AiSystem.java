package dc.targetman.ai;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Iterables;

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
import dclib.util.Maths;

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
				Ai ai = new Ai(entity);
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
		Segment targetSegment = graphHelper.getBelowSegment(targetBounds);
		Segment belowSegment = graphHelper.getBelowSegment(ai.bounds);
		boolean onTargetSegment = targetSegment != null && targetSegment == belowSegment;
		if (onTargetSegment) {
			nextX = targetBounds.getCenter(new Vector2()).x;
		} else if (ai.nextNode != null) {
			nextX = getNextX(ai.bounds, ai.nextNode, belowSegment);
		}
		return nextX;
	}

	private float getNextX(final Rectangle bounds, final DefaultNode node, final Segment belowSegment) {
		float paddingX = 0;
		Segment betweenSegment = getBetweenSegment(bounds, node, belowSegment);
		if (betweenSegment != null) {
			if (betweenSegment.rightNode.x() == node.x()) {
				paddingX = bounds.width;
			} else if (betweenSegment.leftNode.x() == node.x()) {
				paddingX = -bounds.width;
			}
		}
		return node.x() + paddingX;
	}

	private Segment getBetweenSegment(final Rectangle bounds, final DefaultNode node, final Segment belowSegment) {
		Segment betweenSegment = null;
		if (bounds.y < node.y()) {
			betweenSegment = graphHelper.getSegment(node);
		} else if (bounds.y > node.y()) {
			betweenSegment = belowSegment;
		}
		return betweenSegment;
	}

	private void jump(final Ai ai) {
		Segment nextSegment = graphHelper.getSegment(ai.nextNode);
		boolean isNextSegmentDifferent = ai.touchingSegment != null && nextSegment != null
				&& ai.touchingSegment != nextSegment;
		if (isNextSegmentDifferent) {
			boolean doesGapExist = ai.nextNode.y() > ai.bounds.y || !nextSegment.overlapsX(ai.bounds);
			if (doesGapExist) {
				StickActions.jump(ai.entity);
			}
		}
	}

	private void updatePath(final Ai ai, final Rectangle targetBounds) {
		if (ai.entity.get(AiPart.class).checkUpdatePath()) {
			Segment belowSegment = graphHelper.getBelowSegment(ai.bounds);
			Segment targetSegment = graphHelper.getBelowSegment(targetBounds);
			if (belowSegment != null && targetSegment != null) {
				DefaultNode endNode = Iterables.getLast(targetSegment.nodes);
				List<DefaultNode> newPath = graphHelper.createPath(belowSegment, endNode);
				ai.entity.get(AiPart.class).setPath(newPath);
			}
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
	private float getRotateDirection(final Centrum centrum, final Vector2 to, final boolean flipX) {
		final float minAngleOffset = 2;
		float direction = 0;
		Vector2 offset = VectorUtils.offset(centrum.getPosition(), to);
		float angleOffset = Maths.degDistance(offset.angle(), centrum.getRotation());
		if (angleOffset > minAngleOffset) {
			Vector2 fireDirection = new Vector2(1, 0).setAngle(centrum.getRotation());
			direction = offset.y * fireDirection.x > offset.x * fireDirection.y ? 1 : -1;
			if (flipX) {
				direction *= -1;
			}
		}
		return direction;
	}

	private class Ai {

		public final Entity entity;
		public final Rectangle bounds;
		// TODO: Delete.  only used once
		public final Segment touchingSegment;
		public final DefaultNode nextNode;

		public Ai(final Entity entity) {
			this.entity = entity;
			bounds = entity.get(TransformPart.class).getTransform().getBounds();
			DefaultNode touchingNode = graphHelper.getTouchingNode(bounds);
			touchingSegment = graphHelper.getTouchingSegment(bounds);
			List<DefaultNode> path = entity.get(AiPart.class).getPath();
			path.remove(touchingNode);
			nextNode = path.isEmpty() ? null : path.get(0);
		}

	}

}
