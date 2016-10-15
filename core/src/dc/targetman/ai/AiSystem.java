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
				boolean thinking = entity.get(AiPart.class).think();
				Ai ai = new Ai(entity, thinking);
				Rectangle targetBounds = target.get(TransformPart.class).getTransform().getBounds();
				navigate(ai, targetBounds);
				fire(entity, targetBounds);
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

	private void jump(final Ai ai) {
		if (ai.currentNode != null) {
			boolean jumpToNextNode = ai.nextNode != null && ai.nextNode.canJumpTo(ai.bounds.x, ai.bounds.y);
			boolean jumpToCurrentNode = ai.bounds.y + Box2dUtils.ROUNDING_ERROR < ai.currentNode.top();
			if (jumpToNextNode || jumpToCurrentNode) {
				StickActions.jump(ai.entity);
			}
		}
	}

	private float getNextX(final Ai ai, final Rectangle targetBounds) {
		float nextX = Float.NaN;
		if (ai.currentNode == graphHelper.getNearestNode(targetBounds)) {
			nextX = targetBounds.getCenter(new Vector2()).x;
		} else if (!ai.path.isEmpty()) {
			DefaultNode nextNode = ai.path.get(0);
			float edgeOffset = ai.bounds.width  * 1.5f;
			if (ai.bounds.y < nextNode.top()) {
				edgeOffset *= -1;
			}
			if (ai.path.size() > 1 && ai.path.get(1).left() > nextNode.left()) {
				nextX = nextNode.right() - edgeOffset;
			} else {
				nextX = nextNode.left() + edgeOffset;
			}
		}
		return nextX;
	}

	private void updatePath(final Ai ai, final Rectangle targetBounds) {
		if (ai.thinking && ai.currentNode != null) {
			List<DefaultNode> newPath = graphHelper.createPath(ai.currentNode, targetBounds);
			ai.setPath(newPath);
		}
	}

	private void fire(final Entity entity, final Rectangle targetBounds) {
		Centrum centrum = entity.get(WeaponPart.class).getCentrum();
		boolean flipX = entity.get(LimbsPart.class).getFlipX();
		Vector2 targetCenter = targetBounds.getCenter(new Vector2());
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

	private class Ai {

		public final Entity entity;
		public final boolean thinking;
		public final Rectangle bounds;
		public final List<DefaultNode> path;
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
