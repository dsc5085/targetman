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
        return tryGet(name)!!
    }

    fun tryGet(name: String): Limb? {
        val matchingLimbs = getLimbs(true, true).filter { it.name == name }
        val numMatchingLimbs = matchingLimbs.count()
        // TODO: Prevent this state from happening in the first place, then delete this if block
        if (numMatchingLimbs > 1) {
            throw IllegalStateException("There are $numMatchingLimbs limbs with name $name")
        }
        return matchingLimbs.firstOrNull()
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