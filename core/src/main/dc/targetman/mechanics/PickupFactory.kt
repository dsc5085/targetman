package dc.targetman.mechanics

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.util.Json
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.toVector2
import dclib.graphics.TextureCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.system.io.FileUtils
import dclib.util.or

class PickupFactory(
        private val entityManager: EntityManager,
        private val textureCache: TextureCache,
        private val world: World
) {
    fun create(weaponPath: String, position: Vector3) {
        val weapon = Json.toObject<Weapon>(FileUtils.toFileHandle(weaponPath))
        val entity = Entity()
        val region = textureCache.getPolygonRegion(weapon.regionName)
        val heightWidthRatio = region.region.regionHeight.toFloat() / region.region.regionWidth
        val vertices = PolygonUtils.createRectangleVertices(weapon.width, heightWidthRatio * weapon.width)
        val body = Box2dUtils.createDynamicBody(world, vertices)
        body.userData = entity
        Box2dUtils.setFilter(body, CollisionCategory.ALL, CollisionCategory.STATIC.or(CollisionCategory.BOUNDS))
        val transform = Box2dTransform(position.z, body)
        transform.setLocalToWorld(transform.center, position.toVector2())
        entity.attach(TransformPart(transform), SpritePart(region))
        entityManager.add(entity)
    }
}