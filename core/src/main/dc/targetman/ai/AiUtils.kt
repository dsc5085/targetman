package dc.targetman.ai

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World
import dclib.geometry.top

object AiUtils {
    // TODO: Handle sight angle range
    // Determine min and max angle range. min and max is -75 degrees to 75 degrees at the AIs facing direction
    // Check if it hits player
    fun isInSight(from: Vector2, fov: FOV, targetBounds: Rectangle, world: World): Boolean {
        var isSuccessful = true
        val minTargetAngle = targetBounds.getPosition(Vector2()).angle(from)
        val maxTargetAngle = Vector2(targetBounds.x, targetBounds.top).angle(from)
        val minAngle = Math.min(minTargetAngle, fov.angleRange.min)
        val maxAngle = Math.max(maxTargetAngle, fov.angleRange.max)
        val angle = MathUtils.random(minAngle, maxAngle)
        val offset = Vector2(fov.distance, 0f).setAngle(angle)
        val to = from.cpy().add(offset)
        world.rayCast({ fixture, _, _, _ ->
            // TODO: Check alliance and other groups for passthrough?
            if (fixture.body.type === BodyType.StaticBody) {
                isSuccessful = false
                0f
            } else if (/* fixture is enemy bounds */) {
                isSuccessful = true
                0f
            } else {
                -1f
            }
        }, from, to)
        return isSuccessful
    }
}