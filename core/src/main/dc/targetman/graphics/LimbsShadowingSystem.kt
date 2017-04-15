package dc.targetman.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.esotericsoftware.spine.Slot
import dc.targetman.epf.parts.LimbsShadowingPart
import dc.targetman.epf.parts.SkeletonPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.SpritePart

class LimbsShadowingSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val shadowingPart = entity.tryGet(LimbsShadowingPart::class)
        if (shadowingPart != null) {
            val keyLimbNames = shadowingPart.keyLimbNames
            for (i in 0 until keyLimbNames.size) {
                val skeletonPart = entity[SkeletonPart::class]
                val drawOrder = skeletonPart.skeleton.drawOrder
                val shadowingStart = drawOrder.indexOfFirst { it.data.name == keyLimbNames[i] }
                validateSlotIndex(shadowingStart, keyLimbNames[i])
                val shadowingEnd = calculateShadowingEnd(drawOrder, keyLimbNames, i)
                val shadowProgress = i.toFloat() / (shadowingPart.keyLimbNames.size - 1)
                val color = calculateColor(shadowProgress, shadowingPart, skeletonPart.flipX)
                val limbs = skeletonPart.getLimbs(true)
                for (j in shadowingStart until shadowingEnd) {
                    val limb = limbs.first { it.name == drawOrder[j].data.name }
                    limb.entity[SpritePart::class].sprite.color = color
                }
            }
        }
    }

    private fun calculateShadowingEnd(drawOrder: Array<Slot>, keyLimbNames: List<String>, limbNameIndex: Int): Int {
        val shadowingEnd: Int
        val isLastKeyLimb = limbNameIndex >= keyLimbNames.size - 1
        if (isLastKeyLimb) {
            shadowingEnd = drawOrder.size
        } else {
            val keyLimbName = keyLimbNames[limbNameIndex + 1]
            shadowingEnd = drawOrder.indexOfFirst { it.data.name == keyLimbNames[limbNameIndex + 1] }
            validateSlotIndex(shadowingEnd, keyLimbName)
        }
        return shadowingEnd
    }

    private fun calculateColor(shadowProgress: Float, shadowingPart: LimbsShadowingPart, flipX: Boolean): Color {
        val interpolation = if (flipX) shadowProgress else 1f - shadowProgress
        val value = shadowingPart.valueRange.interpolate(interpolation)
        return Color(value, value, value, 1f)
    }

    private fun validateSlotIndex(index: Int, slotName: String) {
        if (index < 0) {
            throw IllegalArgumentException("Entity does not contain slot $slotName")
        }
    }
}