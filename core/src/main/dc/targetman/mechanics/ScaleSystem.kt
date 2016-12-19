package dc.targetman.mechanics

import com.badlogic.gdx.math.Vector2
import dc.targetman.epf.parts.ScalePart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart

class ScaleSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val scalePart = entity.tryGet(ScalePart::class)
        if (scalePart != null) {
            scalePart.scaleTimer.tick(delta)
            val transform = entity[TransformPart::class].transform
            transform.scale = Vector2(scalePart.scaleX, transform.scale.y)
        }
    }
}
