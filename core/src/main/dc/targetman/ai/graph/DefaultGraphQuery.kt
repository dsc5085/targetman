package dc.targetman.ai.graph

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.Heuristic
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.badlogic.gdx.math.Rectangle
import dclib.util.CollectionUtils
import dclib.util.Maths
import org.apache.commons.lang3.StringUtils

class DefaultGraphQuery(private val graph: DefaultIndexedGraph) : GraphQuery {
    private val pathFinder = IndexedAStarPathFinder(graph, true)

    private val heuristic: Heuristic<DefaultNode> = Heuristic { node, toNode ->
        val xOffset = Maths.distance(node.x, toNode.x)
        val yOffset = Maths.distance(toNode.y, node.y)
        xOffset + yOffset
    }

    override fun getNearestNode(x: Float, segment: Segment): DefaultNode {
        return segment.getNodes().minBy { getCost(x, it) }!!
    }

    override fun getNearestBelowSegment(bounds: Rectangle): Segment? {
        val overlappingBelowSegments = graph.getSegments().filter { it.overlapsX(bounds) && it.y < bounds.y }
        return overlappingBelowSegments.minBy { bounds.y - it.y }
    }

    override fun getSegment(node: DefaultNode): Segment {
        val segments = graph.getSegments().filter { it.getNodes().contains(node) }
        if (segments.size > 1) {
            val segmentsString = StringUtils.join(segments)
            throw IllegalStateException("Multiple segments $segmentsString contain node $node")
        }
        return segments.single()
    }

    override fun createPath(fromX: Float, fromSegment: Segment, toNode: DefaultNode): List<DefaultConnection> {
        var lowestCostPath = listOf<DefaultConnection>()
        for (fromNode in fromSegment.getNodes()) {
            val rawPath = DefaultGraphPath<Connection<DefaultNode>>()
            pathFinder.searchConnectionPath(fromNode, toNode, heuristic, rawPath)
            val path = CollectionUtils.getByType(rawPath, DefaultConnection::class)
            if (lowestCostPath.none() || getCost(fromX, path) < getCost(fromX, lowestCostPath)) {
                lowestCostPath = path
            }
        }
        return lowestCostPath.toList()
    }

    private fun getCost(x: Float, path: List<DefaultConnection>): Float {
        var cost = if (path.any()) getCost(x, path.get(0).fromNode) else 0f
        for (i in 0 until path.size) {
            val fromNode = path[i].fromNode
            val toNode = path[i].toNode
            cost += heuristic.estimate(fromNode, toNode)
        }
        return cost
    }

    private fun getCost(x: Float, node: DefaultNode): Float {
        return Maths.distance(x, node.x)
    }
}