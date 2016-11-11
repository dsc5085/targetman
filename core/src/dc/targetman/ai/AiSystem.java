package dc.targetman.ai;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import dc.targetman.epf.parts.AiPart;
import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.mechanics.Direction;
import dc.targetman.mechanics.EntityFinder;
import dc.targetman.mechanics.StickActions;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystem;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.TransformPart;
import dclib.geometry.Centrum;
import dclib.geometry.RectangleUtils;
import dclib.geometry.VectorUtils;
import dclib.util.Maths;

import java.util.ArrayList;
import java.util.List;

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
            Entity target = EntityFinder.INSTANCE.findPlayer(entityManager);
            if (target != null) {
				Ai ai = new Ai(entity);
				Rectangle targetBounds = target.get(TransformPart.class).getTransform().getBounds();
				navigate(ai, targetBounds);
				aim(entity, targetBounds);
                StickActions.trigger(entity);
            }
		}
	}

	private void navigate(final Ai ai, final Rectangle targetBounds) {
		removeReachedNodes(ai);
		Direction moveDirection = getMoveDirection(ai, targetBounds);
		StickActions.move(ai.entity, moveDirection);
		jump(ai, moveDirection);
		updatePath(ai, targetBounds);
	}

	private void removeReachedNodes(final Ai ai) {
		if (ai.belowSegment != null) {
			if (graphHelper.isBelow(ai.nextNode, ai.bounds, ai.belowSegment)) {
				ai.path.remove(ai.nextNode);
				ai.entity.get(AiPart.class).setPath(ai.path);
			}
		}
	}

	private Direction getMoveDirection(final Ai ai, final Rectangle targetBounds) {
		float nextX = getNextX(ai, targetBounds);
		Direction moveDirection = Direction.NONE;
		if (!Float.isNaN(nextX)) {
			if (!RectangleUtils.containsX(ai.bounds, nextX)) {
				float offsetX = nextX - ai.position.x;
				moveDirection = offsetX > 0 ? Direction.RIGHT : Direction.LEFT;
			}
		}
		return moveDirection;
	}

	private float getNextX(final Ai ai, final Rectangle targetBounds) {
		float nextX = Float.NaN;
		Segment targetSegment = graphHelper.getBelowSegment(targetBounds);
		boolean onTargetSegment = targetSegment != null && targetSegment == ai.belowSegment;
		if (onTargetSegment) {
			nextX = RectangleUtils.base(targetBounds).x;
		} else if (ai.nextNode != null) {
			nextX = ai.nextNode.x();
		}
		return nextX;
	}

	private void jump(final Ai ai, final Direction moveDirection) {
		if (ai.belowSegment != null) {
			Rectangle checkBounds = RectangleUtils.grow(ai.bounds, ai.bounds.width / 2, 0);
			boolean atLeftEdge = RectangleUtils.containsX(checkBounds, ai.belowSegment.left());
			boolean atRightEdge = RectangleUtils.containsX(checkBounds, ai.belowSegment.right());
			boolean approachingEdge = (atLeftEdge && moveDirection == Direction.LEFT)
					|| (atRightEdge && moveDirection == Direction.RIGHT);
			Segment nextSegment = graphHelper.getSegment(ai.nextNode);
			boolean notOnNextSegment = nextSegment != null && ai.belowSegment != nextSegment;
			if (approachingEdge || notOnNextSegment) {
				if (ai.nextNode == null || checkBounds.y < ai.nextNode.y()) {
					StickActions.jump(ai.entity);
				}
			}
		}
	}

	private void updatePath(final Ai ai, final Rectangle targetBounds) {
		Segment targetSegment = graphHelper.getBelowSegment(targetBounds);
		if (ai.entity.get(AiPart.class).checkUpdatePath() && ai.belowSegment != null && targetSegment != null) {
			List<DefaultNode> newPath = new ArrayList<DefaultNode>();
			float targetX = RectangleUtils.base(targetBounds).x;
			DefaultNode endNode = graphHelper.getNearestNode(targetX, targetSegment);
			newPath = graphHelper.createPath(ai.position.x, ai.belowSegment, endNode);
			ai.entity.get(AiPart.class).setPath(newPath);
		}
	}

	private void aim(final Entity entity, final Rectangle targetBounds) {
		Centrum centrum = entity.get(WeaponPart.class).getCentrum();
		boolean flipX = entity.get(LimbsPart.class).getFlipX();
		Vector2 targetCenter = targetBounds.getCenter(new Vector2());
		int direction = getRotateDirection(centrum, targetCenter, flipX);
		StickActions.aim(entity, direction);
	}

	/**
	 * Returns float indicating how rotation should change.
	 * @param to to
	 * @param flipX flipX
	 * @return 1 if angle should be increased, -1 if angle should be decreased, or 0 if angle shouldn't change
	 */
	private int getRotateDirection(final Centrum centrum, final Vector2 to, final boolean flipX) {
		final float minAngleOffset = 2;
		int direction = 0;
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

		Entity entity;
		Rectangle bounds;
		Vector2 position;
		Segment belowSegment;
		List<DefaultNode> path;
		DefaultNode nextNode;

		Ai(final Entity entity) {
			this.entity = entity;
			bounds = entity.get(TransformPart.class).getTransform().getBounds();
			position = RectangleUtils.base(bounds);
			belowSegment = graphHelper.getBelowSegment(bounds);
			path = entity.get(AiPart.class).getPath();
			nextNode = path.isEmpty() ? null : path.get(0);
		}

	}

}
