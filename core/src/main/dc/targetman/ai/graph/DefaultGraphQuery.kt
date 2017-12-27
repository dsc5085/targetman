package dc.targetman.ai.graph

import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.GraphPath
import com.badlogic.gdx.ai.pfa.Heuristic
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.badlogic.gdx.math.Rectangle
import dclib.util.Maths
import org.apache.commons.lang3.StringUtils

class DefaultGraphQuery(private val graph: DefaultIndexedGraph) : GraphQuery {
    private val pathFinder = IndexedAStarPathFinder(graph, true)

    private val heuristic: Heuristic<DefaultNode> = Heuristic { node, endNode ->
        val xOffset = Maths.distance(node.x, endNode.x)
        val yOffset = Maths.distance(endNode.y, node.y)
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

    override fun createPath(startX: Float, startSegment: Segment, endNode: DefaultNode): List<DefaultNode> {
        var lowestCostPath: GraphPath<DefaultNode> = DefaultGraphPath()
        for (startNode in startSegment.getNodes()) {
            val path = DefaultGraphPath<DefaultNode>()
            pathFinder.searchNodePath(startNode, endNode, heuristic, path)
            if (lowestCostPath.none() || getCost(startX, path) < getCost(startX, lowestCostPath)) {
                lowestCostPath = path
            }
        }
        return lowestCostPath.toList()
    }

    private fun getCost(x: Float, path: GraphPath<DefaultNode>): Float {
        var cost = if (path.any()) getCost(x, path.get(0)) else 0f
        for (i in 0..path.count - 1 - 1) {
            val startNode = path.get(i)
            val endNode = path.get(i + 1)
            cost += heuristic.estimate(startNode, endNode)
        }
        return cost
    }

    private fun getCost(x: Float, node: DefaultNode): Float {
        return Maths.distance(x, node.x)
    }
}