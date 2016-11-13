package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.physics.limb.LimbUtils

object PhysicsUtils {
    fun applyForce(entities: List<Entity>, target: Entity, force: Vector2) {
        val actualTarget = LimbUtils.findContainer(entities, target) ?: target
        val actualTransform = actualTarget[TransformPart::class.java].transform
        actualTransform.applyImpulse(force)
    }
}