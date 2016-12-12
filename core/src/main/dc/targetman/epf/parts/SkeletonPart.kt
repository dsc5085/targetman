package dc.targetman.epf.parts

import com.esotericsoftware.spine.Skeleton
import dclib.epf.Entity

class SkeletonPart(private val skeleton: Skeleton, private val namesToLimbEntities: Map<String, Entity>) {
    var flipX: Boolean
        get() = skeleton.flipX
        set(value) {
            skeleton.flipX = value
        }

    operator fun get(name: String): Entity {
        return namesToLimbEntities[name]!!
    }

    fun getName(entity: Entity): String {
        return namesToLimbEntities.filter { it.value === entity }.keys.single()
    }

    fun getAll(): Collection<Entity> {
        return namesToLimbEntities.values.filter { it.isActive }
    }

    fun getDescendants(name: String): List<Entity> {
        val entity = get(name)
        val childNames = skeleton.bones.single { it.data.name == name }.children.map { it.data.name }
        val descendants = childNames.flatMap { getDescendants(it) }
        return descendants.plus(entity)
    }
}