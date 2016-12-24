package dc.targetman.epf.parts

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.AnimationStateData
import com.esotericsoftware.spine.Skeleton
import dclib.epf.Entity

class SkeletonPart(val skeleton: Skeleton, val baseScale: Vector2, private val namesToLimbEntities: Map<String, Entity>) {
    val animationState: AnimationState

    init {
        val animationStateData = AnimationStateData(skeleton.data)
        animationState = AnimationState(animationStateData)
    }

    var flipX: Boolean
        get() = baseScale.x < 0
        set(value) {
            val absScaleX = Math.abs(baseScale.x)
            baseScale.x = if (value) -absScaleX else absScaleX
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

    fun playAnimation(name: String) {
        val trackIndex = 0
        if (animationState.tracks.size <= trackIndex || animationState.tracks[trackIndex].animation.name != name) {
            animationState.setAnimation(trackIndex, name, true)
        }
    }
}