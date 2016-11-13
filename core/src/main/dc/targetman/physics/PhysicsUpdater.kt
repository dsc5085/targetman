package dc.targetman.physics

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import dclib.epf.EntityManager
import dclib.physics.Box2dUtils
import dclib.system.Updater

class PhysicsUpdater(private val world: World, entityManager: EntityManager) : Updater {
    private val removedBodies = mutableListOf<Body>()

    init {
        entityManager.entityRemoved.on {
            val body = Box2dUtils.getBody(it.entity)
            if (body != null) {
                removedBodies.add(body)
            }
        }
    }

    override fun update(delta: Float) {
        for (body in removedBodies) {
            world.destroyBody(body)
        }
        removedBodies.clear()
        world.step(delta, 8, 3)
    }
}