package dc.targetman.skeleton

import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity

object LimbUtils {
    fun find(entities: List<Entity>, limbEntity: Entity): Limb? {
        var limb: Limb? = null
        val container = findContainer(entities, limbEntity)
        if (container != null) {
            val skeletonPart = container[SkeletonPart::class]
            limb = if (limbEntity == container) skeletonPart.root else skeletonPart[limbEntity]
        }
        return limb
    }

    fun findContainer(entities: List<Entity>, limbEntity: Entity): Entity? {
        return if (limbEntity.has(SkeletonPart::class)) limbEntity
        else entities.firstOrNull {
            val skeletonPart = it.tryGet(SkeletonPart::class)
            skeletonPart?.getAllLimbs().orEmpty().any { it.entity === limbEntity }
        }
    }
}