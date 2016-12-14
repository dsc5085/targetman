package dc.targetman.mechanics

import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart

class BoundsSyncSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        // TODO: Need better check than this, like check if the transform is in the BOUNDS collision category
        if (entity.has(MovementPart::class.java)) {
            moveLimbsToTransform(entity)
        }
    }

    fun moveLimbsToTransform(entity: Entity) {
        val skeletonPart = entity[SkeletonPart::class.java]
        if (skeletonPart.getActiveLimbs().isNotEmpty()) {
            // TODO: Fix.  Need to find a way to resize bounding box and put limbs at correct location
//            val rootTransform = limbsPart.root.transform
//            val localY = getY(limbsPart) - rootTransform.position.y
//            val bounds = entity[TransformPart::class.java].transform.bounds
//            rootTransform.setGlobal(Vector2(0f, localY), bounds.base)
        }
    }

    private fun getY(skeletonPart: SkeletonPart): Float {
        val limbs = skeletonPart.getActiveLimbs()
        return limbs.map { it[TransformPart::class.java].transform.bounds.y }.minBy { it }!!
    }
}