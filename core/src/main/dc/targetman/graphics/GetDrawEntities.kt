package dc.targetman.graphics

import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.graphics.EntityZComparator

class GetDrawEntities(private val entityManager: EntityManager) : () -> List<Entity> {
    override fun invoke(): List<Entity> {
        val drawGroups = mutableMapOf<Entity, Iterable<Entity>>()
        val entities = entityManager.all.toMutableList()
        addContainerGroups(drawGroups, entities)
        for (entity in entities) {
            drawGroups[entity] = listOf(entity)
        }
        val sortedKeys = drawGroups.keys.sortedWith(EntityZComparator())
        return sortedKeys.flatMap { drawGroups[it]!! }
    }

    private fun addContainerGroups(drawGroups: MutableMap<Entity, Iterable<Entity>>, entities: MutableList<Entity>) {
        val containers = entities.filter { it.has(SkeletonPart::class) }
        for (container in containers) {
            val skeletonPart = container[SkeletonPart::class]
            val sortedChildren = getSortedChildren(skeletonPart)
            val group = sortedChildren + container
            drawGroups[container] = group
            entities.removeAll(group)
        }
    }

    private fun getSortedChildren(skeletonPart: SkeletonPart): List<Entity> {
        val activeLimbs = skeletonPart.getActiveLimbs()
        var sortedLimbs = skeletonPart.skeleton.drawOrder.map {
            slot -> activeLimbs.firstOrNull { limb -> limb.name == slot.data.name }
        }.filterNotNull()
        if (skeletonPart.flipX) {
            sortedLimbs = sortedLimbs.reversed()
        }
        return sortedLimbs.map { it.entity }
    }
}