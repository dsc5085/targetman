package dc.targetman.skeleton

import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity

object LimbUtils {
    fun findContainer(entities: List<Entity>, entityToFind: Entity): Entity? {
        return if (entityToFind.has(SkeletonPart::class)) entityToFind
        else entities.firstOrNull {
            val limbsPart = it.tryGet(SkeletonPart::class)
            limbsPart?.getActiveLimbs().orEmpty().contains(entityToFind)
        }
    }
}