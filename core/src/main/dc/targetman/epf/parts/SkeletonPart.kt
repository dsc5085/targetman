package dc.targetman.epf.parts

import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.AnimationStateData
import dc.targetman.skeleton.Limb

class SkeletonPart(val root: Limb) {
    val skeleton = root.skeleton
    val animationState = createAnimationState()
    val rootScale get() = root.transform.scale

    var flipX
        get() = rootScale.x < 0
        set(value) {
            val scale = rootScale
            scale.x = Math.abs(scale.x) * if (value) -1 else 1
            root.transform.setScale(scale)
        }

    operator fun get(name: String): Limb {
        val limb =  getLimbs(true, true).singleOrNull { it.name == name }
        if (limb == null) {
            throw IllegalArgumentException("Could not find limb ${name}")
        }
        return limb
    }

    fun getLimbs(includeInactive: Boolean = false, includeLinked: Boolean = false): Collection<Limb> {
        return root.getDescendants(includeInactive, includeLinked).plus(root)
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