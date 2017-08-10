package dc.targetman.mechanics

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.World
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import net.dermetfan.gdx.physics.box2d.Box2DUtils

object EntityUtils {
    fun filterSameAlliance(entity: Entity) {
        val alliance = entity.getAttribute(Alliance::class)
        val transform = entity.tryGet(TransformPart::class)?.transform
        if (alliance != null && transform is Box2dTransform) {
            val ignoredGroup = (-Box2dUtils.toGroup(alliance)).toShort()
            Box2dUtils.setFilter(transform.body, group = ignoredGroup)
        }
    }

    fun isGrounded(world: World, entity: Entity): Boolean {
        val body = Box2dUtils.getBody(entity)!!
        return body.linearVelocity.y == 0f && world.contactList.any {
            it.isTouching && (isGroundedContact(body, it.fixtureA, it.fixtureB, it)
                    || isGroundedContact(body, it.fixtureB, it.fixtureA, it))
        }
    }

    private fun isGroundedContact(body: Body, fixture1: Fixture, fixture2: Fixture, contact: Contact): Boolean {
        val legsFixture = body.fixtureList.minBy { Box2DUtils.minYWorld(it) }
        val manifold = contact.worldManifold
        val halfLegsSize = Box2DUtils.height(legsFixture) / 2
        if (legsFixture === fixture1 && !fixture2.isSensor) {
            val maxYForGrounded = Box2DUtils.minYWorld(legsFixture) + halfLegsSize
            return manifold.points.any { it.y < maxYForGrounded }
        }
        return false
    }
}