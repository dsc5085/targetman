package dc.targetman.epf.util;

import java.util.List;

import dc.targetman.epf.parts.MovementPart;
import dc.targetman.epf.parts.WeaponPart;
import dclib.epf.Entity;
import dclib.epf.parts.LimbAnimationsPart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.PhysicsPart;
import dclib.epf.parts.TranslatePart;
import dclib.epf.systems.CollisionSystem;
import dclib.limb.Limb;
import dclib.limb.LimbAnimation;
import dclib.physics.BodyType;
import dclib.physics.Collision;

public final class StickActions {

	private final CollisionSystem collisionSystem;

	public StickActions(final CollisionSystem collisionSystem) {
		this.collisionSystem = collisionSystem;
	}

	public final void move(final Entity entity, final float direction) {
		float moveVelocityX = entity.get(MovementPart.class).getMoveSpeed() * getMoveRatio(entity) * direction;
		entity.get(TranslatePart.class).setVelocityX(moveVelocityX);
		LimbAnimation walkAnimation = entity.get(LimbAnimationsPart.class).get("walk");
		if (moveVelocityX == 0) {
			walkAnimation.stop();
		} else {
			walkAnimation.play();
		}
		if (moveVelocityX > 0) {
			entity.get(LimbsPart.class).setFlipX(false);
		} else if (moveVelocityX < 0) {
			entity.get(LimbsPart.class).setFlipX(true);
		}
	}

	public final void jump(final Entity entity) {
		for (Collision collision : collisionSystem.getCollisions(entity)) {
			PhysicsPart collideePhysicsPart = collision.getCollidee().get(PhysicsPart.class);
			boolean isUpOffset = collision.getOffset().y > 0;
			if (collideePhysicsPart.getBodyType() == BodyType.STATIC && isUpOffset) {
				float jumpSpeed = entity.get(MovementPart.class).getJumpSpeed() * getMoveRatio(entity);
				entity.get(TranslatePart.class).setVelocityY(jumpSpeed);
			}
		}
	}

	public final void aim(final Entity entity, final float direction) {
		entity.get(WeaponPart.class).setAimDirection(direction);
	}

	public final void trigger(final Entity entity) {
		entity.get(WeaponPart.class).setTriggered(true);
	}

	private float getMoveRatio(final Entity entity) {
		int numMovementLimbs = 0;
		List<Limb> movementLimbs = entity.get(MovementPart.class).getLimbs();
		for (Limb limb : entity.get(LimbsPart.class).getRoot().getDescendants()) {
			if (movementLimbs.contains(limb)) {
				numMovementLimbs++;
			}
		}
		return (float)numMovementLimbs / movementLimbs.size();
	}

}