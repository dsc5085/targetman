package dc.targetman.character

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef
import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.StaggerPart
import dc.targetman.mechanics.ActionKey
import dc.targetman.mechanics.ActionsPart
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.EntityUtils
import dc.targetman.mechanics.StaggerState
import dc.targetman.physics.Interactivity
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.collision.CollisionChecker
import dclib.physics.collision.Contacter
import dclib.util.Maths
import net.dermetfan.gdx.physics.box2d.Box2DUtils

class MovementSystem(
        entityManager: EntityManager,
        private val world: World,
        private val collisionChecker: CollisionChecker
) : EntitySystem(entityManager) {
    private val CLIMBING_HAND = "left_hand"

    // TODO: class-level struct to encapsulating moving characters
    override fun update(delta: Float, entity: Entity) {
        if (entity.has(MovementPart::class)) {
            val isGrounded = EntityUtils.isGrounded(collisionChecker, entity)
            move(entity, isGrounded)
            climb(entity)
            updateJumping(entity, isGrounded, delta)
        }
    }

    private fun move(entity: Entity, isGrounded: Boolean) {
        val movementPart = entity[MovementPart::class]
        val actionsPart = entity[ActionsPart::class]
        val direction: Direction
        if (actionsPart[ActionKey.MOVE_RIGHT].doing) {
            direction = Direction.RIGHT
        } else if (actionsPart[ActionKey.MOVE_LEFT].doing) {
            direction = Direction.LEFT
        } else {
            direction = Direction.NONE
        }
        val skeletonPart = entity[SkeletonPart::class]
        updateSkeletonState(isGrounded, direction, skeletonPart)
        val targetVelocityX = getMoveSpeed(movementPart, entity[SkeletonPart::class]).x * direction.toFloat()
        applyMoveImpulse(entity, targetVelocityX)
    }

    private fun updateSkeletonState(isGrounded: Boolean, direction: Direction, skeletonPart: SkeletonPart) {
        if (isGrounded) {
            if (direction == Direction.NONE) {
                skeletonPart.playAnimation("idle")
            } else {
                skeletonPart.playAnimation("run")
            }
        }
        if (direction != Direction.NONE) {
            skeletonPart.flipX = direction === Direction.LEFT
        }
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
        val moveUp = entity[ActionsPart::class][ActionKey.MOVE_UP].doing
        if (!moveUp || jumpIncreaseTimer.isElapsed) {
            jumpIncreaseTimer.reset()
        } else if (moveUp && (isGrounded || jumpIncreaseTimer.isRunning)) {
            jump(entity, isGrounded, delta)
        }
        if (!isGrounded && !movementPart.climbing && entity[TransformPart::class].transform.velocity.y < 0) {
            entity[SkeletonPart::class].playAnimation("fall")
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
        if (!movementPart.climbing) {
            entity[SkeletonPart::class].playAnimation("jump", loop = false)
        }
    }

    private fun climb(entity: Entity) {
        val movementPart = entity[MovementPart::class]
        val collisions = collisionChecker.getCollisions(entity)
        val climbCollision = collisions.firstOrNull { it.target.entity.of(Interactivity.CLIMB) }
        val body = Box2dUtils.getBody(entity)!!
        val climbJoint = body.jointList.map { it.joint }.firstOrNull { it is PrismaticJoint } as PrismaticJoint?
        val actionsPart = entity[ActionsPart::class]
        val dismount = actionsPart[ActionKey.MOVE_LEFT].justDid || actionsPart[ActionKey.MOVE_RIGHT].justDid
        if (climbJoint != null) {
            Box2dUtils.destroyJoint(climbJoint)
        }
        val skeletonPart = entity[SkeletonPart::class]
        if (!dismount && entity[StaggerPart::class].state == StaggerState.OK && climbCollision != null
                && skeletonPart.has(CLIMBING_HAND)) {
            if (actionsPart[ActionKey.MOVE_UP].justDid || actionsPart[ActionKey.MOVE_DOWN].justDid) {
                movementPart.climbing = true
            }
            if (movementPart.climbing) {
                val climbVelocity = calculateClimbVelocity(body, climbCollision.target, movementPart, actionsPart,
                        skeletonPart)
                if (climbVelocity.y == 0f) {
                    skeletonPart.pauseAnimation()
                } else {
                    skeletonPart.playAnimation("climb")
                }
                createClimbJoint(body, climbCollision.target.body, climbVelocity)
            }
        } else {
            movementPart.climbing = false
        }
    }

    private fun calculateClimbVelocity(
            climber: Body,
            climbeable: Contacter,
            movementPart: MovementPart,
            actionsPart: ActionsPart,
            skeletonPart: SkeletonPart
    ): Vector2 {
        val moveSpeed = getMoveSpeed(movementPart, skeletonPart)
        val maxClimbSpeed = moveSpeed.y / 2f
        val climbVelocity = Vector2()
        if (actionsPart[ActionKey.MOVE_UP].doing) {
            climbVelocity.y = maxClimbSpeed
        } else if (actionsPart[ActionKey.MOVE_DOWN].doing) {
            climbVelocity.y = -maxClimbSpeed
        }
        val ladderLeft = Box2DUtils.maxYWorld(climbeable.body) - Box2DUtils.maxYWorld(climber)
        val ladderLeftRatio = ladderLeft / Box2DUtils.size(climber).y
        if (climbVelocity.y > 0) {
            climbVelocity.y = Interpolation.exp5Out.apply(0f, climbVelocity.y, ladderLeftRatio)
        }
        val climbingHandX = skeletonPart[CLIMBING_HAND].transform.center.x
        val climbeableTransform = climbeable.entity[TransformPart::class].transform
        val offsetX = climbeableTransform.center.x - climbingHandX
        val speedX = Interpolation.exp10Out.apply(0f, moveSpeed.x, Math.abs(offsetX) / moveSpeed.x)
        climbVelocity.x = speedX * Math.signum(offsetX)
        return climbVelocity
    }

    private fun createClimbJoint(climber: Body, climbeable: Body, velocity: Vector2) {
        val jointDef = PrismaticJointDef()
        val anchor = Vector2(Box2DUtils.minXWorld(climber), Box2DUtils.minYWorld(climber))
        // In order for joint to work with a velocity length of 0, axis must not be Vector2(0f, 0f), so just set the
        // x-component to an arbitrary value.
        val axis = if (velocity.len() == 0f) Vector2(1f, 0f) else velocity
        jointDef.initialize(climbeable, climber, anchor, axis)
        jointDef.enableMotor = true
        jointDef.collideConnected = true
        // TODO: Bug where if shot off ladder, force causes entity to fly to the moon
        jointDef.maxMotorForce = Float.MAX_VALUE
        jointDef.motorSpeed = velocity.len()
        world.createJoint(jointDef)
    }

    private fun getMoveSpeed(movementPart: MovementPart, skeletonPart: SkeletonPart): Vector2 {
        val movementLimbNames = movementPart.limbNames
        val numActiveMovementLimbs = movementLimbNames.count { skeletonPart.has(it) }
        return movementPart.speed.cpy().scl(numActiveMovementLimbs.toFloat() / movementLimbNames.size)
    }
}