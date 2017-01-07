package dc.targetman.mechanics

import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

object EntityUtils {
    // TODO: Just make a generic get attribute method in Entity
    fun getAlliance(entity: Entity): Alliance? {
        return entity.getAttributes().filterIsInstance<Alliance>().firstOrNull()
    }

    fun filterSameAlliance(entity: Entity) {
        val alliance = EntityUtils.getAlliance(entity)
        val transform = entity.tryGet(TransformPart::class)?.transform
        if (alliance != null && transform is Box2dTransform) {
            val ignoredGroup = (-Box2dUtils.toGroup(alliance)).toShort()
            Box2dUtils.setFilter(transform.body, group = ignoredGroup)
        }
    }
}