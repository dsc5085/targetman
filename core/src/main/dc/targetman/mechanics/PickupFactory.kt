package dc.targetman.mechanics

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.epf.parts.PickupPart
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.mechanics.weapon.WeaponData
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
import dclib.util.inv

class PickupFactory(
        private val entityManager: EntityManager,
        private val textureCache: TextureCache,
        private val world: World
) {
    fun create(weaponPath: String, position: Vector3) {
        val weaponData = Json.toObject<WeaponData>(FileUtils.toFileHandle(weaponPath))
        val entity = Entity()
        val region = textureCache.getPolygonRegion(weaponData.regionName)
        val heightWidthRatio = region.region.regionHeight.toFloat() / region.region.regionWidth
        val vertices = PolygonUtils.createRectangleVertices(weaponData.width, heightWidthRatio * weaponData.width)
        val body = Box2dUtils.createDynamicBody(world, vertices)
        body.userData = entity
        // Ensure that the pickup is always detectable by the characters' collision sensors
        body.isSleepingAllowed = false
        Box2dUtils.setFilter(body, CollisionCategory.ALL, CollisionCategory.BOUNDS.inv())
        val transform = Box2dTransform(position.z, body)
        transform.setLocalToWorld(transform.center, position.toVector2())
        val weapon = Weapon(weaponData)
        entity.attach(TransformPart(transform), SpritePart(region), PickupPart(weapon))
        entityManager.add(entity)
    }
}