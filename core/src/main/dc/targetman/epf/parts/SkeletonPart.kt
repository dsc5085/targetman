package dc.targetman.epf.parts

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.AnimationStateData
import com.esotericsoftware.spine.Skeleton
import dc.targetman.skeleton.Limb
import dclib.epf.Entity

class SkeletonPart(skeleton: Skeleton) {
    var skeleton: Skeleton = Skeleton(skeleton)
    val animationState = createAnimationState()

    private val limbs = mutableListOf<Limb>()

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

    val root: Limb
        get() = get(skeleton.rootBone.data.name)

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

    fun add(limb: Limb) {
        limbs.add(limb)
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