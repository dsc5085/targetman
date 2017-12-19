package dc.targetman.level

import dc.targetman.physics.Interactivity
import dc.targetman.physics.collision.CollisionCategory
import dclib.epf.Entity
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class LevelObjectFactory(private val factoryTools: FactoryTools) {
    fun createLadder() {
        val vertices = PolygonUtils.createRectangleVertices(0.5f, 5f)
        val body = Box2dUtils.createStaticBody(factoryTools.world, vertices)
        Box2dUtils.setFilter(body, null, CollisionCategory.BOUNDS)
        Box2dUtils.setSensor(body, true)
        val transform = Box2dTransform(body)
        val region = factoryTools.textureCache.getPolygonRegion("objects/white")
        val entity = Entity(TransformPart(transform), SpritePart(region))
        entity.addAttributes(Interactivity.LADDER)
        factoryTools.entityManager.add(entity)
    }
}
