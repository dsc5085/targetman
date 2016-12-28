package dc.targetman.epf.parts

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.AnimationStateData
import com.esotericsoftware.spine.Skeleton
import dc.targetman.skeleton.Limb
import dclib.epf.Entity

class SkeletonPart(
        val skeleton: Skeleton,
        val baseScale: Vector2,
        private val limbs: List<Limb>) {
    val animationState = createAnimationState()

    var flipX: Boolean
        get() = baseScale.x < 0
        set(value) {
            val absScaleX = Math.abs(baseScale.x)
            baseScale.x = if (value) -absScaleX else absScaleX
        }

    operator fun get(name: String): Limb {
        return limbs.single { it.name == name }
    }

    operator fun get(entity: Entity): Limb? {
        return limbs.singleOrNull { it.entity === entity }
    }

    fun getAllLimbs(): Collection<Limb> {
        return limbs.toList()
    }

    fun getActiveLimbs(): Collection<Limb> {
        return getAllLimbs().filter { it.isActive }
    }

    fun playAnimation(name: String, trackIndex: Int = 0) {
        val indexExists = trackIndex < animationState.tracks.size
        if (!indexExists || animationState.tracks[trackIndex].animation.name != name) {
            animationState.setAnimation(trackIndex, name, true)
        }
    }

    private fun createAnimationState(): AnimationState {
        val animationStateData = AnimationStateData(skeleton.data)
        return AnimationState(animationStateData)
    }
}