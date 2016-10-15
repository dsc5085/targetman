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
import dclib.physics.Box2dUtils;

// TODO: cleanup.  create helper inner class to represent AI entities
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
		DefaultNode currentNode = graphHelper.getTouchingNode(bounds);
		path.remove(currentNode);
		float moveDirection = getMoveDirection(target, bounds, path, currentNode);
		StickActions.move(entity, moveDirection);
		DefaultNode nextNode = path.isEmpty() ? null : path.get(0);
		jump(entity, currentNode, nextNode);
		think(entity, bounds, target, currentNode);
	}

	private float getMoveDirection(final Entity target, final Rectangle bounds, final List<DefaultNode> path,
		final DefaultNode currentNode) {
		int moveDirection = 0;
		Rectangle targetBounds = target.get(TransformPart.class).getTransform().getBounds();
		float nextX = Float.NaN;
		if (currentNode == graphHelper.getNearestNode(targetBounds)) {
			nextX = targetBounds.getCenter(new Vector2()).x;
		} else if (!path.isEmpty()) {
			nextX = getNextX(bounds, path);
		}
		if (!Float.isNaN(nextX)) {
			float offsetX = nextX - bounds.getCenter(new Vector2()).x;
			if (Math.abs(offsetX) > bounds.width / 2) {
				moveDirection = offsetX > 0 ? 1 : -1;
			}
		}
		return moveDirection;
	}

	private void think(final Entity entity, final Rectangle bounds, final Entity target,
			final DefaultNode currentNode) {
		if (currentNode != null && entity.get(AiPart.class).think()) {
			Rectangle targetBounds = target.get(TransformPart.class).getTransform().getBounds();
			List<DefaultNode> newPath = graphHelper.createPath(currentNode, targetBounds);
			entity.get(AiPart.class).setPath(newPath);
		}
	}

	private float getNextX(final Rectangle bounds, final List<DefaultNode> path) {
		float nextX = Float.NaN;
		DefaultNode nextNode = path.get(0);
		float edgeOffset = bounds.width  * 1.5f;
		if (bounds.y < nextNode.top()) {
			edgeOffset *= -1;
		}
		if (path.size() > 1 && path.get(1).left() > nextNode.left()) {
			nextX = nextNode.right() - edgeOffset;
		} else {
			nextX = nextNode.left() + edgeOffset;
		}
		return nextX;
	}

	private void jump(final Entity entity, final DefaultNode currentNode, final DefaultNode nextNode) {
		Rectangle bounds = entity.get(TransformPart.class).getTransform().getBounds();
		if (currentNode != null) {
			boolean jumpToNextNode = nextNode != null && nextNode.canJumpTo(bounds.x, bounds.y);
			boolean jumpToCurrentNode = bounds.y + Box2dUtils.ROUNDING_ERROR < currentNode.top();
			if (jumpToNextNode || jumpToCurrentNode) {
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
//		StickActions.trigger(entity);
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

}
