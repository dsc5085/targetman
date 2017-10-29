package dc.targetman.epf.parts

import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.AnimationStateData
import dc.targetman.skeleton.Limb
import dc.targetman.skeleton.LimbLink
import dc.targetman.skeleton.LinkType

class SkeletonPart(val root: LimbLink) {
    var isEnabled = true
    val skeleton = root.limb.skeleton
    val animationState = createAnimationState()

    var flipX
        get() = root.scale.x < 0
        set(value) {
            root.scale.x = Math.abs(root.scale.x) * if (value) -1 else 1
        }

    // TODO: Move these getters to Limb
    fun has(name: String): Boolean {
        return tryGet(name) != null
    }

    operator fun get(name: String): Limb {
        return tryGet(name)!!
    }

    fun tryGet(name: String): Limb? {
        return getLimbs().filter { it.name == name }.firstOrNull()
    }

    fun getLimbs(vararg linkTypes: LinkType = LinkType.values()): Collection<Limb> {
        return root.limb.getDescendants(*linkTypes)
    }

    fun setMix(fromName: String, toName: String, duration: Float) {
        animationState.data.setMix(fromName, toName, duration)
    }

    fun playAnimation(name: String, trackIndex: Int = 0) {
        val indexExists = trackIndex < animationState.tracks.size
        if (!indexExists || animationState.tracks[trackIndex].animation.name != name) {
            animationState.setAnimation(trackIndex, name, true)
        }
    }

    private fun createAnimationState(): AnimationState {
        val animationStateData = AnimationStateData(skeleton.data)
        animationStateData.defaultMix = 2f
        return AnimationState(animationStateData)
    }
}