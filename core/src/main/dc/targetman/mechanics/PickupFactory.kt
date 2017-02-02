package dc.targetman.mechanics

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.epf.parts.PickupPart
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.skeleton.SkeletonUtils
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.toVector2
import dclib.graphics.TextureCache
import dclib.graphics.TextureUtils
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.util.inv

class PickupFactory(
        private val entityManager: EntityManager,
        private val textureCache: TextureCache,
        private val world: World
) {
    fun create(weapon: Weapon, position: Vector3) {
        val size = weapon.size
        val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
        val body = Box2dUtils.createDynamicBody(world, vertices)
        val transform = Box2dTransform(body, position.z)
        transform.setLocalToWorld(transform.center, position.toVector2())
        create(weapon, transform)
    }

    fun create(weapon: Weapon, weaponTransform: Box2dTransform) {
        val entity = Entity()
        val atlas = textureCache.getAtlas(weapon.data.atlasName)
        val skeleton = SkeletonUtils.createSkeleton(weapon.data.skeletonPath, atlas)
        val regionAttachment = SkeletonUtils.getRegionAttachments(skeleton.slots).first()
        val region = TextureUtils.createPolygonRegion(regionAttachment.region)
        setup(weaponTransform.body)
        entity.attach(TransformPart(weaponTransform), SpritePart(region), PickupPart(weapon))
        entityManager.add(entity)
    }

    private fun setup(body: Body) {
        for (fixture in body.fixtureList) {
            fixture.isSensor = false
        }
        // Ensure that the pickup is always detectable by the characters' collision sensors
        body.isSleepingAllowed = false
        body.gravityScale = 1f
        Box2dUtils.setFilter(body, CollisionCategory.ALL, CollisionCategory.BOUNDS.inv(), 0)
    }
}