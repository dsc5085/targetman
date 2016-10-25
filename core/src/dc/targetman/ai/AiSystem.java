package dc.targetman.ai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Iterables;

import dc.targetman.epf.parts.AiPart;
import dc.targetman.epf.parts.MovementPart;
import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.mechanics.Alliance;
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
		int moveDirection = getMoveDirection(ai, targetBounds);
		StickActions.move(ai.entity, moveDirection);
		jump(ai, moveDirection);
		updatePath(ai, targetBounds);
	}

	private int getMoveDirection(final Ai ai, final Rectangle targetBounds) {
		// TODO: Create enum for moveDirection
		float nextX = getNextX(ai, targetBounds);
		int moveDirection = 0;
		if (!Float.isNaN(nextX)) {
			float offsetX = nextX - ai.bounds.getCenter(new Vector2()).x;
			if (Math.abs(offsetX) > getCheckBounds(ai.bounds).width / 2) {
				moveDirection = offsetX > 0 ? 1 : -1;
			}
		} else if (ai.belowSegment == null) {
			moveDirection = ai.entity.get(MovementPart.class).getDirection();
		}
		return moveDirection;
	}

	private float getNextX(final Ai ai, final Rectangle targetBounds) {
		float nextX = Float.NaN;
		Segment targetSegment = graphHelper.getBelowSegment(targetBounds);
		boolean onTargetSegment = targetSegment != null && targetSegment == ai.belowSegment;
		if (onTargetSegment) {
			nextX = targetBounds.getCenter(new Vector2()).x;
		} else if (ai.nextNode != null) {
			nextX = getNextX(ai.bounds, ai.nextNode, ai.belowSegment);
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

	private void jump(final Ai ai, final int moveDirection) {
		if (ai.belowSegment != null) {
			Rectangle checkBounds = getCheckBounds(ai.bounds);
			boolean atLeftEdge = RectangleUtils.containsX(checkBounds, ai.belowSegment.leftNode.x());
			boolean atRightEdge = RectangleUtils.containsX(checkBounds, ai.belowSegment.rightNode.x());
			boolean approachingEdge = (atLeftEdge && moveDirection < 0) || (atRightEdge && moveDirection > 0);
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
		if (ai.entity.get(AiPart.class).checkUpdatePath()) {
			Segment targetSegment = graphHelper.getBelowSegment(targetBounds);
			List<DefaultNode> newPath = new ArrayList<DefaultNode>();
			if (ai.belowSegment != null && targetSegment != null) {
				DefaultNode endNode = Iterables.getLast(targetSegment.nodes);
				newPath = graphHelper.createPath(ai.belowSegment, endNode);
			}
			ai.entity.get(AiPart.class).setPath(newPath);
		}
	}

	private Rectangle getCheckBounds(final Rectangle bounds) {
		return RectangleUtils.grow(bounds, bounds.width / 2);
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
	 * @param from from
	 * @param to to
	 * @param currentAngle currentAngle
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
		Segment belowSegment;
		DefaultNode nextNode;

		Ai(final Entity entity) {
			this.entity = entity;
			Rectangle bounds = entity.get(TransformPart.class).getTransform().getBounds();
			this.bounds = bounds;
			belowSegment = graphHelper.getBelowSegment(bounds);
			List<DefaultNode> path = entity.get(AiPart.class).getPath();
			if (belowSegment != null) {
				List<DefaultNode> belowNodes = graphHelper.getBelowNodes(bounds, belowSegment);
				path.removeAll(belowNodes);
			}
			nextNode = path.isEmpty() ? null : path.get(0);
		}

	}

}
