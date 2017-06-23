package dc.targetman.epf.parts

import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.AnimationStateData
import dc.targetman.skeleton.Limb
import dc.targetman.skeleton.SkeletonRoot

class SkeletonPart(val root: SkeletonRoot) {
    var isEnabled = true
    val skeleton = root.limb.skeleton
    val animationState = createAnimationState()

    var flipX
        get() = root.scale.x < 0
        set(value) {
            root.scale.x = Math.abs(root.scale.x) * if (value) -1 else 1
        }

    operator fun get(name: String): Limb {
        val limb =  tryGet(name)
        return limb ?: throw IllegalArgumentException("Could not find limb or there were multiple limbs called $name")
    }

    fun tryGet(name: String): Limb? {
        return getLimbs(true, true).singleOrNull { it.name == name }
    }

    fun getLimbs(includeInactive: Boolean = false, includeLinked: Boolean = false): Collection<Limb> {
        return root.limb.getDescendants(includeInactive, includeLinked)
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