package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.skeleton.LimbUtils
import dclib.epf.Entity
import dclib.epf.parts.TransformPart

object PhysicsUtils {
    fun createWorld(): World {
        return World(Vector2(0f, -10f), true)
    }

    fun applyForce(entities: List<Entity>, target: Entity, force: Vector2) {
        val actualTarget = LimbUtils.findContainer(entities, target) ?: target
        val actualTransform = actualTarget[TransformPart::class].transform
        actualTransform.applyImpulse(force)
    }
}