package dc.targetman.character

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Joint
import com.badlogic.gdx.physics.box2d.JointEdge
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
        val targetVelocityX = movementPart.speed.x * getMoveStrength(entity) * direction.toFloat()
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
        val maxJumpSpeed = movementPart.speed.y * getMoveStrength(entity)
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
        val ladderJointEdge = body.jointList.firstOrNull { it.joint is PrismaticJoint }
        if (ladderCollision != null) {
            if (movementPart.tryMoveUp || movementPart.tryMoveDown) {
                movementPart.climbingLadder = true
            }
            updateClimbSpeed(ladderJointEdge?.joint, movementPart)
            if (movementPart.climbingLadder && ladderJointEdge == null) {
                createLadderJoint(body, ladderCollision.target.body)
            }
        } else {
            getOffLadder(movementPart, ladderJointEdge)
        }
    }

    private fun updateClimbSpeed(joint: Joint?, movementPart: MovementPart) {
        if (joint is PrismaticJoint) {
            val climbSpeed: Float
            if (movementPart.tryMoveUp) {
                climbSpeed = 10f
            } else if (movementPart.tryMoveDown) {
                climbSpeed = -10f
            } else {
                climbSpeed = 0f
            }
            joint.motorSpeed = climbSpeed
        }
    }

    private fun getOffLadder(movementPart: MovementPart, ladderJointEdge: JointEdge?) {
        movementPart.climbingLadder = false
        if (ladderJointEdge != null) {
            Box2dUtils.destroyJoint(ladderJointEdge.joint)
        }
    }

    private fun createLadderJoint(climber: Body, ladder: Body) {
        val jointDef = PrismaticJointDef()
        val anchor = Vector2(Box2DUtils.minXWorld(climber), Box2DUtils.minYWorld(climber))
        jointDef.initialize(ladder, climber, anchor, Vector2(0f, 1f))
        jointDef.enableLimit = true
        jointDef.enableMotor = true
        jointDef.upperTranslation = 100f
        jointDef.lowerTranslation = -10000f
        jointDef.collideConnected = true
        jointDef.maxMotorForce = 200f
        world.createJoint(jointDef)
    }

    private fun getMoveStrength(entity: Entity): Float {
        val movementLimbNames = entity[MovementPart::class].limbNames
        val skeletonPart = entity[SkeletonPart::class]
        val numActiveMovementLimbs = movementLimbNames.count { skeletonPart.has(it) }
        return numActiveMovementLimbs.toFloat() / movementLimbNames.size
    }
}