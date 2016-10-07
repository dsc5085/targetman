package dc.targetman.ai;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dc.targetman.epf.parts.AiPart;
import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.gamelogic.StickActions;
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
				navigate(entity, target);
				fire(entity, target);
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

	private void navigate(final Entity entity, final Entity target) {
		Rectangle bounds = entity.get(TransformPart.class).getTransform().getBounds();
		List<DefaultNode> path = entity.get(AiPart.class).getPath();
		DefaultNode currentNode = graphHelper.getNode(bounds);
		if (!path.isEmpty() && path.get(0).equals(currentNode)) {
			path.remove(currentNode);
		}
		float moveDirection = getMoveDirection(target, bounds, path, currentNode);
		StickActions.move(entity, moveDirection);
		DefaultNode nextNode = path.isEmpty() ? null : path.get(0);
		jump(entity, nextNode);
		think(entity, bounds, target, nextNode);
	}

	private float getMoveDirection(final Entity target, final Rectangle bounds, final List<DefaultNode> path,
		final DefaultNode currentNode) {
		int moveDirection = 0;
		Rectangle targetBounds = target.get(TransformPart.class).getTransform().getBounds();
		float nextX = Float.NaN;
		if (currentNode == graphHelper.getNearestNode(targetBounds)) {
			nextX = targetBounds.getCenter(new Vector2()).x;
		} else if (!path.isEmpty()) {
			nextX = getNextX(path);
		}
		if (!Float.isNaN(nextX)) {
			moveDirection = nextX > bounds.getCenter(new Vector2()).x ? 1 : -1;
		}
		return moveDirection;
	}

	private void think(final Entity entity, final Rectangle bounds, final Entity target, final DefaultNode nextNode) {
		if (entity.get(AiPart.class).think()) {
			entity.get(AiPart.class).getPath();
			DefaultNode startNode = nextNode == null ? graphHelper.getNode(bounds) : nextNode;
			Rectangle targetBounds = target.get(TransformPart.class).getTransform().getBounds();
			List<DefaultNode> newPath = graphHelper.createPath(startNode, targetBounds);
			if (!newPath.isEmpty()) {
				entity.get(AiPart.class).setPath(newPath);
			}
		}
	}

	private float getNextX(final List<DefaultNode> path) {
		// TODO: Doesn't handle if nextnextnode x is in middle of nextNode
		DefaultNode nextNode = path.get(0);
		float nextX = nextNode.x();
		if (path.size() > 1) {
			DefaultNode nextNextNode = path.get(1);
			if (nextNextNode.x() > nextNode.x()) {
				nextX = nextNode.right();
			}
		}
		return nextX;
	}

	private void jump(final Entity entity, final DefaultNode nextNode) {
		if (nextNode != null) {
			Rectangle bounds = entity.get(TransformPart.class).getTransform().getBounds();
			if (nextNode.canJumpTo(bounds.x, RectangleUtils.right(bounds), bounds.y)) {
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
