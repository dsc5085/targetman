package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.epf.parts.MovementPart
import dc.targetman.physics.JumpChecker
import dc.targetman.physics.JumpVelocitySolver
import dc.targetman.physics.PhysicsUtils
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils

object GraphQueryFactory {
    fun create(boundsList: List<Rectangle>, aiEntity: Entity): GraphQuery {
        val movementPart = aiEntity[MovementPart::class.java]
        val jumpVelocitySolver = JumpVelocitySolver(Vector2(movementPart.moveSpeed, movementPart.jumpSpeed))
        val size = aiEntity[TransformPart::class.java].transform.size
        val staticWorld = PhysicsUtils.createWorld()
        for (boundsVertices in boundsList.map { PolygonUtils.createRectangleVertices(it) }) {
            PhysicsUtils.createStaticBody(staticWorld, boundsVertices)
        }
        val jumpChecker = JumpChecker(staticWorld, jumpVelocitySolver)
        val graph = GraphFactory(boundsList, size, jumpChecker).create()
        staticWorld.dispose()
        return DefaultGraphQuery(graph)
    }
}