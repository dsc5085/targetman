package dc.targetman.skeleton

import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity

object LimbUtils {
    fun find(entities: Collection<Entity>, limbEntity: Entity): Limb? {
        var limb: Limb? = null
        val container = findContainer(entities, limbEntity)
        if (container != null) {
            val skeletonPart = container[SkeletonPart::class]
            limb = if (limbEntity == container) skeletonPart.root.limb
            else skeletonPart.getLimbs(true, true).singleOrNull { it.entity === limbEntity }
        }
        return limb
    }

    fun findContainer(entities: Collection<Entity>, limbEntity: Entity): Entity? {
        return if (limbEntity.has(SkeletonPart::class)) limbEntity
        else entities.firstOrNull {
            val skeletonPart = it.tryGet(SkeletonPart::class)
            skeletonPart?.getLimbs(true).orEmpty().any { it.entity === limbEntity }
        }
    }

    fun findParent(entities: Collection<Entity>, limb: Limb): Limb? {
        for (entity in entities) {
            val skeletonPart = entity.tryGet(SkeletonPart::class)
            if (skeletonPart != null) {
                val limbs = skeletonPart.getLimbs( includeLinked = true)
                val parentLimb = limbs.firstOrNull { it.getChildren(true, true).contains(limb) }
                if (parentLimb != null) {
                    return parentLimb
                }
            }
        }
        return null
    }
}