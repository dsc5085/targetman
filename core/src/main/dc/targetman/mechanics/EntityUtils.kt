package dc.targetman.mechanics

import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

object EntityUtils {
    fun filterSameAlliance(entity: Entity) {
        val alliance = entity.getAttribute(Alliance::class)
        val transform = entity.tryGet(TransformPart::class)?.transform
        if (alliance != null && transform is Box2dTransform) {
            val ignoredGroup = (-Box2dUtils.toGroup(alliance)).toShort()
            Box2dUtils.setFilter(transform.body, group = ignoredGroup)
        }
    }
}