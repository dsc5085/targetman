package dc.targetman.ai

import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.GraphPath
import com.badlogic.gdx.ai.pfa.Heuristic
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.level.MapUtils
import dclib.geometry.containsX
import dclib.graphics.ScreenHelper
import dclib.util.Maths
import java.util.*

class GraphHelper(map: TiledMap, screenHelper: ScreenHelper, actorSize: Vector2) {
    private val graph = createGraph(map, screenHelper, actorSize)
    private val pathFinder = IndexedAStarPathFinder(graph, true)

    private val heuristic: Heuristic<DefaultNode> = Heuristic { node, endNode ->
        val xOffset = Maths.distance(node.x(), endNode.x())
        val yOffset = Maths.distance(endNode.y(), node.y())
        xOffset + yOffset
    }

    fun getNearestNode(x: Float, segment: Segment): DefaultNode {
        return segment.nodes.minBy { getCost(x, it) }!!
    }

    fun isBelow(node: DefaultNode?, bounds: Rectangle, belowSegment: Segment): Boolean {
        return belowSegment.nodes.contains(node) && bounds.containsX(node!!.x())
    }

    fun getNearestBelowSegment(bounds: Rectangle): Segment? {
        val overlappingBelowSegments = graph.segments.filter { it.overlapsX(bounds) && it.y < bounds.y }
        return overlappingBelowSegments.minBy { bounds.y - it.y }
    }

    fun getSegment(node: DefaultNode?): Segment? {
        return graph.segments.singleOrNull { it.nodes.contains(node) }
    }

    fun createPath(x: Float, startSegment: Segment, endNode: DefaultNode): List<DefaultNode> {
        var lowestCostPath: GraphPath<DefaultNode> = DefaultGraphPath()
        for (startNode in startSegment.nodes) {
            val path = DefaultGraphPath<DefaultNode>()
            pathFinder.searchNodePath(startNode, endNode, heuristic, path)
            if (lowestCostPath.none() || getCost(x, path) < getCost(x, lowestCostPath)) {
                lowestCostPath = path
            }
        }
        return lowestCostPath.toList()
    }

    private fun createGraph(map: TiledMap, screenHelper: ScreenHelper, actorSize: Vector2): DefaultIndexedGraph {
        val collisionLayer = MapUtils.getCollisionLayer(map)
        val boundsList = ArrayList<Rectangle>()
        val size = screenHelper.toWorldUnits(collisionLayer.tileWidth, collisionLayer.tileHeight)
        for (y in 0..collisionLayer.height - 1 - 1) {
            var x = 0
            while (x < collisionLayer.width) {
                val floorLength = getFloorLength(collisionLayer, x, y)
                if (floorLength > 0) {
                    val bounds = Rectangle(x.toFloat(), y.toFloat(), floorLength * size.x, size.y)
                    boundsList.add(bounds)
                    x += floorLength
                }
                x++
            }
        }
        return DefaultIndexedGraph(boundsList, actorSize)
    }

    private fun getFloorLength(layer: TiledMapTileLayer, x: Int, y: Int): Int {
        var floorLength = 0
        var i = x
        while (i < layer.width && layer.getCell(i, y) != null && layer.getCell(i, y + 1) == null) {
            floorLength++
            i++
        }
        return floorLength
    }

    private fun getCost(x: Float, path: GraphPath<DefaultNode>): Float {
        var cost = if (path.count > 0) getCost(x, path.get(0)) else 0f
        for (i in 0..path.count - 1 - 1) {
            val startNode = path.get(i)
            val endNode = path.get(i + 1)
            cost += heuristic.estimate(startNode, endNode)
        }
        return cost
    }

    private fun getCost(x: Float, node: DefaultNode): Float {
        return Maths.distance(x, node.x())
    }
}
