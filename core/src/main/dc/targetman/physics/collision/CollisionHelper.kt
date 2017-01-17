package dc.targetman.physics.collision

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import dclib.geometry.PolygonUtils
import dclib.geometry.Segment2
import dclib.physics.Transform
import dclib.system.Advancer

object CollisionHelper {
    fun getEdgeContact(dynamicTransform: Transform, staticTransform: Transform): EdgeContact? {
        val moveEnd = dynamicTransform.center
        val moveMaxDistance = dynamicTransform.velocity.scl(Advancer.MAX_UPDATE_DELTA)
        val moveStart = moveEnd.cpy().sub(moveMaxDistance)
        val moveEdge = Segment2(moveStart, moveEnd)
        val staticEdges = PolygonUtils.getEdges(staticTransform.getVertices())
        val intersection = Vector2()
        val staticEdge = staticEdges.firstOrNull {
            Intersector.intersectSegments(it.a, it.b, moveEdge.a, moveEdge.b, intersection)
        }
        return if (staticEdge != null) EdgeContact(staticEdge, intersection) else null
    }
}