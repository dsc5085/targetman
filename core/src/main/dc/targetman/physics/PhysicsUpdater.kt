package dc.targetman.physics

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.physics.Box2dUtils
import dclib.system.Updater

class PhysicsUpdater(
        private val world: World,
        entityManager: EntityManager,
        bodyDestroyFilter: (Entity) -> Boolean
) : Updater {
    private val bodiesToDestroy = mutableListOf<Body>()

    init {
        entityManager.entityDestroyed.on {
            val body = Box2dUtils.getBody(it.entity)
            if (body != null && bodyDestroyFilter(it.entity)) {
                bodiesToDestroy.add(body)
            }
        }
    }

    override fun update(delta: Float) {
        for (body in bodiesToDestroy) {
            world.destroyBody(body)
        }
        bodiesToDestroy.clear()
        world.step(delta, Box2dUtils.VELOCITY_ITERATIONS, Box2dUtils.POSITION_ITERATIONS)
    }
}