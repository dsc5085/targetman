package dc.targetman.mechanics.weapon

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import dc.targetman.epf.parts.CounterDeathPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.graphics.TextureCache
import dclib.physics.DefaultTransform
import dclib.physics.Transform

class WeaponEffectsFactory(private val entityManager: EntityManager, private val textureCache: TextureCache) {
    private val flashNamespaceToNames = mutableMapOf<String, List<String>>()

    fun create(effectsData: WeaponEffectsData, muzzleTransform: Transform) {
        val namespace = effectsData.flashNamespace
        val names = flashNamespaceToNames.getOrPut(namespace, { textureCache.getNames(namespace) })
        val name = names[MathUtils.random(names.size - 1)]
        val region = textureCache.getPolygonRegion(name)
        val entity = Entity()
        val heightRatio = region.region.regionHeight.toFloat() / region.region.regionWidth.toFloat()
        val width = effectsData.flashLength
        val height = effectsData.flashLength * heightRatio
        val vertices = PolygonUtils.createRectangleVertices(width, height)
        val transform = DefaultTransform(Polygon(vertices), 0f)
        transform.rotation = muzzleTransform.rotation
        transform.setLocalToWorld(Vector2(width / 6f, height / 2f), muzzleTransform.center)
        entity.attach(TransformPart(transform))
        entity.attach(SpritePart(region))
        entity.attach(CounterDeathPart(2))
        entityManager.add(entity)
    }
}