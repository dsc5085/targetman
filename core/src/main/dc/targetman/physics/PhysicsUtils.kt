package dc.targetman.physics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.physics.limb.LimbUtils
import net.dermetfan.gdx.math.BayazitDecomposer

object PhysicsUtils {
    fun createWorld(): World {
        return World(Vector2(0f, -10f), true)
    }

    fun createBody(world: World, type: BodyDef.BodyType, vertices: FloatArray, sensor: Boolean): Body {
        val bodyDef = BodyDef()
        bodyDef.type = type
        val body = world.createBody(bodyDef)
        val vectors = PolygonUtils.toVectors(vertices).toTypedArray()
        val vertexVectors = com.badlogic.gdx.utils.Array<Vector2>(vectors)
        for (partition in BayazitDecomposer.convexPartition(vertexVectors)) {
            val shape = PolygonShape()
            shape.set(PolygonUtils.toFloats(partition.toList()))
            val fixture = body.createFixture(shape, 1f)
            fixture.isSensor = sensor
            shape.dispose()
        }
        return body
    }

    fun applyForce(entities: List<Entity>, target: Entity, force: Vector2) {
        val actualTarget = LimbUtils.findContainer(entities, target) ?: target
        val actualTransform = actualTarget[TransformPart::class.java].transform
        actualTransform.applyImpulse(force)
    }
}