package dc.targetman.mechanics

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.physics.collision.CollisionCategory
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.TransformPart
import dclib.graphics.ConvexHullCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class PickupFactory(val entityManager: EntityManager, val convexHullCache: ConvexHullCache,  val world: World) {
    fun create(weapon: Weapon, position: Vector3) {
        val entity = Entity()
        val hull = convexHullCache.create(weapon.regionName)
        val body = Box2dUtils.createDynamicBody(world, hull.hull)
        body.userData = entity
        Box2dUtils.setFilter(body, CollisionCategory.ALL)
        val transform = Box2dTransform(0f, body)
        entity.attach(TransformPart(transform))
        entityManager.add(entity)
    }
}