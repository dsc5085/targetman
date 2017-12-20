package dc.targetman.character

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef
import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.EntityUtils
import dc.targetman.physics.Interactivity
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.collision.CollisionChecker
import dclib.util.Maths
import net.dermetfan.gdx.physics.box2d.Box2DUtils

class MovementSystem(
        entityManager: EntityManager,
        private val world: World,
        private val collisionChecker: CollisionChecker
) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        if (entity.has(MovementPart::class)) {
            val isGrounded = EntityUtils.isGrounded(collisionChecker, entity)
            move(entity, isGrounded)
            updateJumping(entity, isGrounded, delta)
            climbLadder(entity)
            if (!isGrounded && entity[TransformPart::class].transform.velocity.y < 0) {
                entity[SkeletonPart::class].playAnimation("fall")
            }
        }
    }

    private fun move(entity: Entity, isGrounded: Boolean) {
        val movementPart = entity[MovementPart::class]
        val direction = movementPart.direction
        val skeletonPart = entity[SkeletonPart::class]
        val targetVelocityX = getMoveSpeed(movementPart, entity[SkeletonPart::class]).x * direction.toFloat()
        if (isGrounded) {
            if (direction == Direction.NONE) {
                skeletonPart.playAnimation("idle")
            } else {
                skeletonPart.playAnimation("run")
            }
        }
        if (direction != Direction.NONE) {
            entity[SkeletonPart::class].flipX = direction === Direction.LEFT
        }
        applyMoveImpulse(entity, targetVelocityX)
    }

    private fun applyMoveImpulse(entity: Entity, targetVelocityX: Float) {
        val maxImpulseScale = 0.7f
        val transform = entity[TransformPart::class].transform as Box2dTransform
        val mass = transform.body.mass
        val targetImpulse = Box2dUtils.getImpulseToReachVelocity(transform.velocity.x, targetVelocityX, mass)
        val impulse = Maths.minAbs(targetImpulse, maxImpulseScale * mass)
        transform.applyImpulse(Vector2(impulse, 0f))
    }

    private fun updateJumping(entity: Entity, isGrounded: Boolean, delta: Float) {
        val movementPart = entity[MovementPart::class]
        val jumpIncreaseTimer = movementPart.jumpIncreaseTimer
        if (!movementPart.tryMoveUp || jumpIncreaseTimer.isElapsed) {
            jumpIncreaseTimer.reset()
        } else if (movementPart.tryMoveUp && (isGrounded || jumpIncreaseTimer.isRunning)) {
            jump(entity, isGrounded, delta)
        }
    }

    private fun jump(entity: Entity, isGrounded: Boolean, delta: Float) {
        val movementPart = entity[MovementPart::class]
        val transform = entity[TransformPart::class].transform as Box2dTransform
        if (isGrounded) {
            transform.velocity = Vector2(transform.velocity.x, 0f)
        }
        val jumpIncreaseTimer = movementPart.jumpIncreaseTimer
        val maxJumpSpeed = getMoveSpeed(movementPart, entity[SkeletonPart::class]).y
        val oldApproxJumpSpeed = maxJumpSpeed * jumpIncreaseTimer.elapsedPercent
        jumpIncreaseTimer.tick(delta)
        val newApproxJumpSpeedWithGravity = maxJumpSpeed * jumpIncreaseTimer.elapsedPercent + delta * -world.gravity.y
        val impulseY = Box2dUtils.getImpulseToReachVelocity(oldApproxJumpSpeed, newApproxJumpSpeedWithGravity,
                transform.body.mass)
        transform.applyImpulse(Vector2(0f, impulseY))
        entity[SkeletonPart::class].playAnimation("jump", loop = false)
    }

    private fun climbLadder(entity: Entity) {
        val movementPart = entity[MovementPart::class]
        val collisions = collisionChecker.getCollisions(entity)
        val ladderCollision = collisions.firstOrNull { it.target.entity.of(Interactivity.LADDER) }
        val body = Box2dUtils.getBody(entity)!!
        val ladderJoint = body.jointList.map { it.joint }.firstOrNull { it is PrismaticJoint } as PrismaticJoint?
        if (ladderJoint != null) {
            Box2dUtils.destroyJoint(ladderJoint)
        }
        if (ladderCollision != null) {
            if (movementPart.tryMoveUp || movementPart.tryMoveDown) {
                movementPart.climbingLadder = true
            }
            if (movementPart.climbingLadder) {
                createLadderJoint(body, ladderCollision.target.body, movementPart, entity[SkeletonPart::class])
            }
        } else {
            movementPart.climbingLadder = false
        }
    }

    private fun createLadderJoint(climber: Body, ladder: Body, movementPart: MovementPart, skeletonPart: SkeletonPart) {
        val maxClimbSpeed = getMoveSpeed(movementPart, skeletonPart).x / 2f
        val climbVelocity = Vector2()
        if (movementPart.tryMoveUp) {
            climbVelocity.y = 1f
        } else if (movementPart.tryMoveDown) {
            climbVelocity.y = -1f
        }
        if (movementPart.direction == Direction.RIGHT) {
            climbVelocity.x = 1f
        } else if (movementPart.direction == Direction.LEFT) {
            climbVelocity.x = -1f
        }
        climbVelocity.setLength(maxClimbSpeed)
        val bodyHeightAboveLadder = Box2DUtils.maxYWorld(climber) - Box2DUtils.maxYWorld(ladder)
        val bodyRatioAboveLadder = bodyHeightAboveLadder / Box2DUtils.size(climber).y
        climbVelocity.y = Interpolation.exp5Out.apply(0f, climbVelocity.y, 1f - bodyRatioAboveLadder)
        createLadderJoint(climber, ladder, climbVelocity)
    }

    private fun createLadderJoint(climber: Body, ladder: Body, velocity: Vector2) {
        val jointDef = PrismaticJointDef()
        val anchor = Vector2(Box2DUtils.minXWorld(climber), Box2DUtils.minYWorld(climber))
        // In order for joint to work with a velocity length of 0, axis must not be Vector2(0f, 0f), so just set the
        // x-component to an arbitrary value.
        val axis = if (velocity.len() == 0f) Vector2(1f, 0f) else velocity
        jointDef.initialize(ladder, climber, anchor, axis)
        jointDef.enableLimit = true
        jointDef.enableMotor = true
        jointDef.upperTranslation = 100f
        jointDef.lowerTranslation = -10000f
        jointDef.collideConnected = true
        jointDef.maxMotorForce = 200f
        jointDef.motorSpeed = velocity.len()
        world.createJoint(jointDef)
    }

    private fun getMoveSpeed(movementPart: MovementPart, skeletonPart: SkeletonPart): Vector2 {
        val movementLimbNames = movementPart.limbNames
        val numActiveMovementLimbs = movementLimbNames.count { skeletonPart.has(it) }
        return movementPart.speed.cpy().scl(numActiveMovementLimbs.toFloat() / movementLimbNames.size)
    }
}