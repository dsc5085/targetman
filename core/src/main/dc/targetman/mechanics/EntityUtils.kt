package dc.targetman.mechanics

import com.badlogic.gdx.physics.box2d.Body
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.collision.Collision
import dclib.physics.collision.CollisionChecker
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

    fun isGrounded(collisionChecker: CollisionChecker, body: Body): Boolean {
        return body.linearVelocity.y == 0f && collisionChecker.getCollisions(body).any {
            it.isTouching && isGroundedContact(body, it)
        }
    }

    private fun isGroundedContact(body: Body, collision: Collision): Boolean {
        val legsFixture = body.fixtureList.minBy { Box2DUtils.minYWorld(it) }
        val halfLegsSize = Box2DUtils.height(legsFixture) / 2
        if (!collision.target.fixture.isSensor && legsFixture === collision.source.fixture) {
            val maxYForGrounded = Box2DUtils.minYWorld(legsFixture) + halfLegsSize
            return collision.manifold.any { it.y < maxYForGrounded }
        }
        return false
    }
}