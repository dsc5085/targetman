package dc.targetman.mechanics

import dclib.epf.Entity

object EntityUtils {
    fun getAlliance(entity: Entity): Alliance? {
        return entity.getAttributes().filterIsInstance<Alliance>().firstOrNull()
    }
}