package dc.targetman.epf.parts

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.AnimationStateData
import com.esotericsoftware.spine.Skeleton
import dc.targetman.skeleton.Limb

class SkeletonPart(val skeleton: Skeleton, val root: Limb) {
    val animationState = createAnimationState()

    val baseScale: Vector2
        get() {
            return root.transform.scale
        }

    var flipX: Boolean
        get() = baseScale.x < 0
        set(value) {
            val scale = baseScale
            scale.x = Math.abs(scale.x) * if (value) -1 else 1
            root.transform.scale = scale
        }

    operator fun get(name: String): Limb {
        return getLimbs(true).single { it.name == name }
    }

    fun getLimbs(includeInactive: Boolean = false): Collection<Limb> {
        return root.getDescendants(includeInactive)
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