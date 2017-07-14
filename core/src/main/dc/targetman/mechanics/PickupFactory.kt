package dc.targetman.mechanics

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import dc.targetman.epf.parts.PickupPart
import dc.targetman.level.FactoryTools
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.skeleton.SkeletonFactory
import dc.targetman.skeleton.SkeletonUtils
import dclib.epf.Entity
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.toVector2
import dclib.graphics.TextureUtils
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class PickupFactory(private val factoryTools: FactoryTools) {
    private val skeletonFactory = SkeletonFactory(factoryTools.textureCache)

    fun create(weapon: Weapon, position: Vector3) {
        val size = weapon.size
        val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
        val body = Box2dUtils.createDynamicBody(factoryTools.world, vertices)
        val transform = Box2dTransform(body, position.z)
        transform.setLocalToWorld(transform.center, position.toVector2())
        create(weapon, transform)
    }

    fun create(weapon: Weapon, weaponTransform: Box2dTransform) {
        val entity = Entity()
        val skeleton = skeletonFactory.create(weapon.data.skeletonPath, weapon.data.atlasName)
        val regionAttachment = SkeletonUtils.getRegionAttachments(skeleton.slots).first()
        val region = TextureUtils.createPolygonRegion(regionAttachment.region)
        setup(weaponTransform.body)
        entity.attach(TransformPart(weaponTransform), SpritePart(region), PickupPart(weapon))
        factoryTools.entityManager.add(entity)
    }

    private fun setup(body: Body) {
        Box2dUtils.setSensor(body, false)
        // Ensures that the pickup is always detectable by the characters' collision sensors
        body.isSleepingAllowed = false
        Box2dUtils.setFilter(body, mask = CollisionCategory.STATIC, group = 0)
    }
}