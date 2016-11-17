package dc.targetman.mechanics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.epf.parts.MovementPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.LimbAnimationsPart
import dclib.epf.parts.LimbsPart
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.util.Timer
import net.dermetfan.gdx.physics.box2d.Box2DUtils

class MovementSystem(entityManager: EntityManager, private val world: World) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        if (entity.has(MovementPart::class.java)) {
            move(entity)
            updateJumping(entity, delta)
        }
    }

    private fun move(entity: Entity) {
        val movementPart = entity[MovementPart::class.java]
        val direction = movementPart.direction
        val walkAnimation = entity[LimbAnimationsPart::class.java]["walk"]
        var targetVelocityX = movementPart.moveSpeed * getMoveStrength(entity) * direction.toFloat()
        if (direction === Direction.NONE) {
            walkAnimation.stop()
        } else {
            walkAnimation.play()
            entity[LimbsPart::class.java].flipX = direction === Direction.LEFT
        }
        applyMoveImpulse(entity, targetVelocityX)
    }

    private fun applyMoveImpulse(entity: Entity, targetVelocityX: Float) {
        val maxImpulseScale = 0.7f
        val transform = entity[TransformPart::class.java].transform as Box2dTransform
        val mass = transform.body.mass
        val targetImpulse = Box2dUtils.getImpulseToReachVelocity(transform.velocity.x, targetVelocityX, mass)
        val maxImpulseAbs = maxImpulseScale * mass
        val impulse = Math.min(Math.abs(targetImpulse), maxImpulseAbs) * Math.signum(targetImpulse)
        transform.applyImpulse(Vector2(impulse, 0f))
    }

    private fun updateJumping(entity: Entity, delta: Float) {
        val movementPart = entity[MovementPart::class.java]
        if (movementPart.tryJumping) {
            increaseJump(entity, delta)
        } else {
            movementPart.jumpIncreaseTimer.reset()
        }
        movementPart.tryJumping = false
    }

    private fun increaseJump(entity: Entity, delta: Float) {
        val transform = entity[TransformPart::class.java].transform as Box2dTransform
        val movementPart = entity[MovementPart::class.java]
        val jumpIncreaseTimer = movementPart.jumpIncreaseTimer
        if (isGrounded(entity) && !jumpIncreaseTimer.isStarted) {
            transform.velocity = Vector2(transform.velocity.x, 0f)
            increaseJump(delta, entity, jumpIncreaseTimer, movementPart, transform)
        } else if (jumpIncreaseTimer.isStarted) {
            increaseJump(delta, entity, jumpIncreaseTimer, movementPart, transform)
        }
    }

    private fun increaseJump(delta: Float, entity: Entity, jumpIncreaseTimer: Timer, movementPart: MovementPart, transform: Box2dTransform) {
        val maxJumpSpeed = movementPart.jumpSpeed * getMoveStrength(entity)
        val oldApproxJumpSpeed = maxJumpSpeed * jumpIncreaseTimer.elapsedPercent
        jumpIncreaseTimer.tick(delta)
        val newApproxJumpSpeedWithGravity = maxJumpSpeed * jumpIncreaseTimer.elapsedPercent + delta * -world.gravity.y
        val impulseY = Box2dUtils.getImpulseToReachVelocity(oldApproxJumpSpeed, newApproxJumpSpeedWithGravity,
                transform.body.mass)
        transform.applyImpulse(Vector2(0f, impulseY))
        jumpIncreaseTimer.check()
    }

    private fun isGrounded(entity: Entity): Boolean {
        val body = Box2dUtils.getBody(entity)!!
        return body.linearVelocity.y === 0f && world.contactList.any {
            val fixtureA = it.fixtureA
            val fixtureB = it.fixtureB
            it.isTouching && (isGroundedContact(body, fixtureA, fixtureB, it)
                    || isGroundedContact(body, fixtureB, fixtureA, it))
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
        val movementLimbs = entity[MovementPart::class.java].limbs
        val numActiveMovementLimbs = entity[LimbsPart::class.java].all.count { movementLimbs.contains(it) }
        return numActiveMovementLimbs.toFloat() / movementLimbs.size
    }
}