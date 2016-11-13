package dc.targetman.ai

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World

object AiUtils {
    // TODO: Handle sight angle range
    fun isInSight(from: Vector2, to: Vector2, maxDistance: Float, world: World): Boolean {
        return from.dst(to) < maxDistance && isRayCastSuccessful(from, to, world)
    }

    private fun isRayCastSuccessful(from: Vector2, to: Vector2, world: World): Boolean {
        var isSuccessful = true
        world.rayCast({ fixture, point, normal, fraction ->
            // TODO: Check alliance and other groups for passthrough?
            if (fixture.body.type === BodyType.StaticBody) {
                isSuccessful = false
                0f
            } else {
                -1f
            }
        }, from, to)
        return isSuccessful
    }
}