package dc.targetman.ai

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.RayCastCallback
import dclib.geometry.top
import dclib.physics.collision.CollisionChecker

object AiUtils {
    fun isTargetInSight(agent: Agent, collisionChecker: CollisionChecker): Boolean {
        // TODO: Need to take into account facing direction
        var isSuccessful = true
        val targetBounds = agent.targetBounds
        val fov = agent.aiPart.profile.fov
        val eye = agent.eye
        val minTargetAngle = targetBounds.getPosition(Vector2()).angle(eye)
        val maxTargetAngle = Vector2(targetBounds.x, targetBounds.top).angle(eye)
        val minAngle = Math.min(minTargetAngle, fov.angleRange.min)
        val maxAngle = Math.max(maxTargetAngle, fov.angleRange.max)
        val angle = MathUtils.random(minAngle, maxAngle)
        val offset = Vector2(fov.distance, 0f).setAngle(angle)
        val to = eye.cpy().add(offset)
        collisionChecker.rayCast(RayCastCallback { fixture, _, _, _ ->
            if (fixture.body.type === BodyType.StaticBody) {
                isSuccessful = false
                0f
            } else if (agent.target === collisionChecker.bodyToEntityMap[fixture.body]) {
                println("FOUND")
                isSuccessful = true
                0f
            } else {
                -1f
            }
        }, eye, to)
        return isSuccessful
    }
}