package dc.targetman.skeleton

import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity

object LimbUtils {
    fun find(entities: List<Entity>, entityToFind: Entity): Limb? {
        var limb: Limb? = null
        val container = findContainer(entities, entityToFind)
        if (container != null) {
            val skeletonPart = container[SkeletonPart::class]
            limb = if (entityToFind == container) skeletonPart.root else skeletonPart[entityToFind]
        }
        return limb
    }

    fun findContainer(entities: List<Entity>, entityToFind: Entity): Entity? {
        return if (entityToFind.has(SkeletonPart::class)) entityToFind
        else entities.firstOrNull {
            val skeletonPart = it.tryGet(SkeletonPart::class)
            skeletonPart?.getAllLimbs().orEmpty().any { it.entity === entityToFind }
        }
    }
}