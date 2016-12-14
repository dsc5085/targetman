package dc.targetman.character

import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity

object LimbUtils {
    fun findContainer(entities: List<Entity>, entityToFind: Entity): Entity? {
        return if (entityToFind.has(SkeletonPart::class.java)) entityToFind
        else entities.firstOrNull {
            val limbsPart = it.tryGet(SkeletonPart::class.java)
            limbsPart?.getActiveLimbs().orEmpty().contains(entityToFind)
        }
    }
}