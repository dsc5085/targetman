package dc.targetman.physics

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.character.DeathForm
import dclib.epf.EntityManager
import dclib.physics.Box2dUtils
import dclib.system.Updater

class PhysicsUpdater(private val world: World, entityManager: EntityManager) : Updater {
    private val removedBodies = mutableListOf<Body>()

    init {
        entityManager.entityRemoved.on {
            val body = Box2dUtils.getBody(it.entity)
            // TODO: pass in the business-specific CORPSE check to this constructor to avoid mixing of business logic
            if (body != null && !it.entity.of(DeathForm.CORPSE)) {
                removedBodies.add(body)
            }
        }
    }

    override fun update(delta: Float) {
        for (body in removedBodies) {
            world.destroyBody(body)
        }
        removedBodies.clear()
        world.step(delta, Box2dUtils.VELOCITY_ITERATIONS, Box2dUtils.POSITION_ITERATIONS)
    }
}