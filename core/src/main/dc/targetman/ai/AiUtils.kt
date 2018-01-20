package dc.targetman.ai

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.RayCastCallback
import dc.targetman.mechanics.Direction
import dclib.physics.collision.CollisionChecker
import dclib.util.FloatRange
import dclib.util.Maths

object AiUtils {
    fun isTargetInSight(agent: Agent, collisionChecker: CollisionChecker): Boolean {
        val fov = agent.aiPart.profile.fov
        val eye = agent.eye
        val angle = getSightAngle(fov.angleRange, agent.facingDirection)
        val offset = Vector2(fov.distance, 0f).setAngle(angle)
        val to = eye.cpy().add(offset)
        var staticFraction = Float.POSITIVE_INFINITY
        var targetFraction = Float.POSITIVE_INFINITY
        collisionChecker.rayCast(RayCastCallback { fixture, _, _, fraction ->
            val returnValue: Float
            if (fixture.isSensor) {
                returnValue = -1f
            } else if (fixture.body.type === BodyType.StaticBody) {
                staticFraction = Math.min(fraction, staticFraction)
                returnValue = fraction
            } else if (agent.target === collisionChecker.bodyToEntityMap[fixture.body]) {
                targetFraction = Math.min(fraction, targetFraction)
                returnValue = fraction
            } else {
                returnValue = -1f
            }
            if (staticFraction < targetFraction) {
                0f
            } else {
                returnValue
            }
        }, eye, to)
        return targetFraction < staticFraction
    }

    private fun getSightAngle(angleRange: FloatRange, facingDirection: Direction): Float {
        val baseAngle = angleRange.random()
        if (facingDirection == Direction.RIGHT) {
            return baseAngle
        } else {
            val scale = Vector2(-1f, 1f)
            return Maths.getScaledRotation(baseAngle, scale)
        }
    }
}