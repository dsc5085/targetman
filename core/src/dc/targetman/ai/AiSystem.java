package dc.targetman.ai;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Iterables;

import dc.targetman.epf.parts.AiPart;
import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.gamelogic.EntityUtils;
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

	// TODO: Don't update every frame
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
			Entity target = getTarget(entity);
			if (target != null) {
				navigate(entity, target);
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

	private void navigate(final Entity entity, final Entity target) {
		if (entity.get(AiPart.class).think()) {
			updatePath(entity, target);
		}
		Vector2 base = EntityUtils.getBase(entity);
		List<DefaultNode> path = entity.get(AiPart.class).getPath();
		DefaultNode currentNode = graphHelper.getNode(base);
		if (!path.isEmpty() && path.get(0).equals(currentNode)) {
			path.remove(currentNode);
		}
		if (!path.isEmpty()) {
			DefaultNode nextNode = path.get(0);
			int moveDirection = nextNode.x() > base.x ? 1 : -1;
			StickActions.move(entity, moveDirection);
			jump(entity, currentNode, moveDirection);
		}
	}

	private void updatePath(final Entity entity, final Entity target) {
		Vector2 base = EntityUtils.getBase(entity);
		Vector2 targetBase = EntityUtils.getBase(target);
		List<DefaultNode> newPath = graphHelper.createPath(base, targetBase);
		List<DefaultNode> path = entity.get(AiPart.class).getPath();
		if (newPath.isEmpty() || path.isEmpty() || !Iterables.getLast(newPath).equals(Iterables.getLast(path))) {
			entity.get(AiPart.class).setPath(newPath);
			System.out.println(newPath.get(0));
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
