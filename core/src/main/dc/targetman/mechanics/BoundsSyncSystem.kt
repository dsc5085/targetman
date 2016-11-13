package dc.targetman.mechanics

import com.badlogic.gdx.math.Vector2
import dc.targetman.epf.parts.MovementPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.LimbsPart
import dclib.epf.parts.TransformPart
import dclib.geometry.base

class BoundsSyncSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        // TODO: Need better check than this, like check if the transform is in the BOUNDS collision category
        if (entity.has(MovementPart::class.java)) {
            moveLimbsToTransform(entity)
        }
    }

    fun moveLimbsToTransform(entity: Entity) {
        val limbsPart = entity[LimbsPart::class.java]
        if (limbsPart.root.descendants.size > 0) {
            val rootTransform = limbsPart.root.transform
            val localY = getY(limbsPart) - rootTransform.position.y
            val bounds = entity[TransformPart::class.java].transform.bounds
            rootTransform.setGlobal(Vector2(0f, localY), bounds.base)
        }
    }

    private fun getY(limbsPart: LimbsPart): Float {
        val descendants = limbsPart.root.descendants
        return descendants.map { it.transform.bounds.y }.minBy { it }!!
    }
}