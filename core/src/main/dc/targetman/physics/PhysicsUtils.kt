package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.skeleton.LimbUtils
import dclib.epf.Entity
import dclib.epf.parts.TransformPart

object PhysicsUtils {
    fun createWorld(): World {
        return World(Vector2(0f, -18f), true)
    }

    fun applyForce(entities: Collection<Entity>, target: Entity, force: Vector2) {
        target[TransformPart::class].transform.applyImpulse(force)
        val targetContainer = LimbUtils.findContainer(entities, target)
        if (targetContainer != null) {
            targetContainer[TransformPart::class].transform.applyImpulse(force)
        }
    }
}