package dc.targetman.physics

import com.badlogic.gdx.physics.box2d.World
import dclib.eventing.EventDelegate
import dclib.physics.collision.ContactedEvent

class Simulator(private val world: World) {
    val contacted = EventDelegate<ContactedEvent>()

    fun run(maxTime: Float, delta: Float) {
        var currentTime = 0f
        while (currentTime < maxTime) {
            checkCollisions()
            currentTime = Math.min(currentTime + delta, maxTime)
        }
    }

    private fun checkCollisions() {
        for (contact in world.contactList.filter { it.isTouching }) {

        }
    }
}