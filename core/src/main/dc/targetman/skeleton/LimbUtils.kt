package dc.targetman.skeleton

import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity

object LimbUtils {
    fun find(entities: Collection<Entity>, limbEntity: Entity): Limb? {
        var limb: Limb? = null
        val container = findContainer(entities, limbEntity)
        if (container != null) {
            val skeletonPart = container[SkeletonPart::class]
            limb = if (limbEntity == container) skeletonPart.root
            else skeletonPart.getLimbs().singleOrNull { it.entity === limbEntity }
        }
        return limb
    }

    fun findContainer(entities: Collection<Entity>, limbEntity: Entity): Entity? {
        return if (limbEntity.has(SkeletonPart::class)) limbEntity
        else entities.firstOrNull {
            val skeletonPart = it.tryGet(SkeletonPart::class)
            skeletonPart?.getLimbs().orEmpty().any { it.entity === limbEntity }
        }
    }
}