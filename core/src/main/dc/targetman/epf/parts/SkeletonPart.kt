package dc.targetman.epf.parts

import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.AnimationStateData
import com.esotericsoftware.spine.Skeleton
import dclib.epf.Entity

class SkeletonPart(val skeleton: Skeleton, private val namesToLimbEntities: Map<String, Entity>) {
    val animationState: AnimationState

    init {
        val animationStateData = AnimationStateData(skeleton.data)
        animationState = AnimationState(animationStateData)
    }

    var flipX: Boolean
        get() = skeleton.rootBone.scaleX < 0
        set(value) {
            val absScaleX = Math.abs(skeleton.rootBone.scaleX)
            skeleton.rootBone.scaleX = if (value) -absScaleX else absScaleX
        }

    operator fun get(name: String): Entity {
        return namesToLimbEntities[name]!!
    }

    fun getName(entity: Entity): String {
        return namesToLimbEntities.filter { it.value === entity }.keys.single()
    }

    fun getAllLimbs(): Collection<Entity> {
        return namesToLimbEntities.values
    }

    fun getActiveLimbs(): Collection<Entity> {
        return getAllLimbs().filter { it.isActive }
    }

    fun getDescendants(name: String): List<Entity> {
        val entity = get(name)
        val childNames = skeleton.bones.single { it.data.name == name }.children.map { it.data.name }
        val descendants = childNames.flatMap { getDescendants(it) }
        return descendants.plus(entity)
    }
}