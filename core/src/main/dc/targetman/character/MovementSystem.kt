package dc.targetman.character

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.Direction
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import net.dermetfan.gdx.physics.box2d.Box2DUtils

class MovementSystem(entityManager: EntityManager, private val world: World) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        if (entity.has(MovementPart::class)) {
            move(entity)
            updateJumping(entity, delta)
        }
    }

    private fun move(entity: Entity) {
        val movementPart = entity[MovementPart::class]
        val direction = movementPart.direction
        val skeletonPart = entity[SkeletonPart::class]
        var targetVelocityX = movementPart.moveSpeed * getMoveStrength(entity) * direction.toFloat()
        if (direction == Direction.NONE) {
            skeletonPart.playAnimation("idle")
        } else {
            skeletonPart.playAnimation("run")
            entity[SkeletonPart::class].flipX = direction === Direction.LEFT
        }
        applyMoveImpulse(entity, targetVelocityX)
    }

    private fun applyMoveImpulse(entity: Entity, targetVelocityX: Float) {
        val maxImpulseScale = 0.7f
        val transform = entity[TransformPart::class].transform as Box2dTransform
        val mass = transform.body.mass
        val targetImpulse = Box2dUtils.getImpulseToReachVelocity(transform.velocity.x, targetVelocityX, mass)
        val maxImpulseAbs = maxImpulseScale * mass
        val impulse = Math.min(Math.abs(targetImpulse), maxImpulseAbs) * Math.signum(targetImpulse)
        transform.applyImpulse(Vector2(impulse, 0f))
    }

    private fun updateJumping(entity: Entity, delta: Float) {
        val movementPart = entity[MovementPart::class]
        val jumpIncreaseTimer = movementPart.jumpIncreaseTimer
        if (!movementPart.tryJumping || jumpIncreaseTimer.isElapsed) {
            jumpIncreaseTimer.reset()
        } else if (movementPart.tryJumping && (isGrounded(entity) || jumpIncreaseTimer.isStarted)) {
            jump(entity, delta)
        }
    }

    private fun jump(entity: Entity, delta: Float) {
        val movementPart = entity[MovementPart::class]
        val transform = entity[TransformPart::class].transform as Box2dTransform
        if (isGrounded(entity)) {
            transform.velocity = Vector2(transform.velocity.x, 0f)
        }
        val jumpIncreaseTimer = movementPart.jumpIncreaseTimer
        val maxJumpSpeed = movementPart.jumpSpeed * getMoveStrength(entity)
        val oldApproxJumpSpeed = maxJumpSpeed * jumpIncreaseTimer.elapsedPercent
        jumpIncreaseTimer.tick(delta)
        val newApproxJumpSpeedWithGravity = maxJumpSpeed * jumpIncreaseTimer.elapsedPercent + delta * -world.gravity.y
        val impulseY = Box2dUtils.getImpulseToReachVelocity(oldApproxJumpSpeed, newApproxJumpSpeedWithGravity,
                transform.body.mass)
        transform.applyImpulse(Vector2(0f, impulseY))
    }

    private fun isGrounded(entity: Entity): Boolean {
        val body = Box2dUtils.getBody(entity)!!
        return body.linearVelocity.y == 0f && world.contactList.any {
            it.isTouching && (isGroundedContact(body, it.fixtureA, it.fixtureB, it)
                    || isGroundedContact(body, it.fixtureB, it.fixtureA, it))
        }
    }

    private fun isGroundedContact(body: Body, fixture1: Fixture, fixture2: Fixture, contact: Contact): Boolean {
        val legsFixture = body.fixtureList.minBy { Box2DUtils.minYWorld(it) }
        val manifold = contact.worldManifold
        val halfLegsSize = Box2DUtils.height(legsFixture) / 2
        if (legsFixture === fixture1 && !fixture2.isSensor) {
            val maxYForGrounded = Box2DUtils.minYWorld(legsFixture) + halfLegsSize
            return manifold.points.any { it.y < maxYForGrounded }
        }
        return false
    }

    private fun getMoveStrength(entity: Entity): Float {
        val movementLimbNames = entity[MovementPart::class].limbNames
        val skeletonPart = entity[SkeletonPart::class]
        val numActiveMovementLimbs = movementLimbNames.count { skeletonPart[it].isActive }
        return numActiveMovementLimbs.toFloat() / movementLimbNames.size
    }
}