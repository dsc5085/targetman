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
import dc.targetman.mechanics.ClimbMode
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
            /** TODO: On autocentering (onto a climbeable object)
             * If we're close enough to the ladder
             * Get the destination position to center on the ladder
             * Automatically move the entity closer to the ladder center
             * If other inputs are pressed, get out of this mode
             * Once at the ladder center, go to climb mode
             */
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
        val transform = entity[TransformPart::class].transform
        if (!isGrounded && movementPart.climbMode == ClimbMode.OFF && transform.velocity.y < 0) {
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
        if (movementPart.climbMode == ClimbMode.OFF) {
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
        if (!dismount && entity[StaggerPart::class].state == StaggerState.OK && climbCollision != null) {
            val climbeableBody = climbCollision.target.body
            if (actionsPart[ActionKey.MOVE_UP].justDid || actionsPart[ActionKey.MOVE_DOWN].justDid) {
                movementPart.climbMode = ClimbMode.ON
            }
            if (movementPart.climbMode == ClimbMode.ON) {
                val climbVelocity = calculateClimbVelocity(body, climbeableBody, movementPart, actionsPart,
                        entity[SkeletonPart::class])
                if (!climbVelocity.isZero) {
                    entity[SkeletonPart::class].playAnimation("climb")
                } else {
                    entity[SkeletonPart::class].pauseAnimation()
                }
                createClimbJoint(body, climbeableBody, climbVelocity)
            }
        } else {
            movementPart.climbMode = ClimbMode.OFF
        }
    }

    private fun calculateClimbVelocity(
            climber: Body,
            climbeable: Body,
            movementPart: MovementPart,
            actionsPart: ActionsPart,
            skeletonPart: SkeletonPart
    ): Vector2 {
        val maxClimbSpeed = getMoveSpeed(movementPart, skeletonPart).x / 2f
        val climbVelocity = Vector2()
        if (actionsPart[ActionKey.MOVE_UP].doing) {
            climbVelocity.y = 1f
        } else if (actionsPart[ActionKey.MOVE_DOWN].doing) {
            climbVelocity.y = -1f
        }
        climbVelocity.setLength(maxClimbSpeed)
        val ladderLeft = Box2DUtils.maxYWorld(climbeable) - Box2DUtils.maxYWorld(climber)
        val ladderLeftRatio = ladderLeft / Box2DUtils.size(climber).y
        if (climbVelocity.y > 0) {
            climbVelocity.y = Interpolation.exp5Out.apply(0f, climbVelocity.y, ladderLeftRatio)
        }
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