package dc.targetman.ai.graph

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import dc.targetman.util.ArrayUtils
import dclib.util.Maths
import kotlin.comparisons.compareBy

class DefaultIndexedGraph(boundsList: List<Rectangle>, private val actorSize: Vector2) : IndexedGraph<DefaultNode> {
    private val segments = createSegments(boundsList)
    private val nodes = mutableListOf<DefaultNode>()

    init {
        connect(segments)
        nodes.addAll(segments.flatMap { it.nodes })
    }

    override fun getConnections(fromNode: DefaultNode): Array<Connection<DefaultNode>> {
        return ArrayUtils.toGdxArray(fromNode.connections)
    }

    override fun getNodeCount(): Int {
        return nodes.size
    }

    override fun getIndex(node: DefaultNode): Int {
        return nodes.indexOf(node)
    }

    fun getSegments(): List<Segment> {
        return segments.toList()
    }

    private fun createSegments(boundsList: List<Rectangle>): List<Segment> {
        return boundsList.map { Segment(it) }
    }

    private fun connect(segments: List<Segment>) {
        for (i in 0..segments.size - 1 - 1) {
            val segment1 = segments[i]
            for (j in i + 1..segments.size - 1) {
                val segment2 = segments[j]
                connect(segment1, segment2)
            }
        }
        for (segment in segments) {
            connectWithin(segment)
        }
    }

    private fun connect(segment1: Segment, segment2: Segment) {
        connect(segment1.leftNode, segment2.rightNode)
        connect(segment1.rightNode, segment2.leftNode)
        connect(segment2.leftNode, segment1.rightNode)
        connect(segment2.rightNode, segment1.leftNode)
        connectMiddle(segment1, segment2)
        connectMiddle(segment2, segment1)
    }

    private fun connectMiddle(topSegment: Segment, bottomSegment: Segment) {
        if (topSegment.y > bottomSegment.y) {
            connectMiddle(topSegment.leftNode, bottomSegment, -actorSize.x)
            connectMiddle(topSegment.rightNode, bottomSegment, actorSize.x)
        }
    }

    private fun connectMiddle(topNode: DefaultNode, bottomSegment: Segment, landingOffsetX: Float) {
        val landingX = topNode.x() + landingOffsetX
        if (bottomSegment.containsX(landingX)) {
            val bottomNode = DefaultNode(landingX, bottomSegment.y)
            bottomSegment.nodes.add(bottomNode)
            connect(topNode, bottomNode)
            connect(bottomNode, topNode)
        }
    }

    private fun connect(startNode: DefaultNode, endNode: DefaultNode) {
        if (canJumpTo(startNode, endNode)) {
            startNode.addConnection(endNode)
        }
    }

    private fun canJumpTo(startNode: DefaultNode, endNode: DefaultNode): Boolean {
        // TODO: Replace these constants with calculations
        val jumpWidth = 8f
        val jumpHeight = 5f
        val gapWidth = Maths.distance(startNode.x(), endNode.x())
        val canJumpToHorizontally = gapWidth < jumpWidth
        val yOffset = endNode.y() - startNode.y()
        val canJumpToVertically = yOffset < jumpHeight
        return canJumpToHorizontally && canJumpToVertically
    }

    private fun connectWithin(segment: Segment) {
        val sortedNodes = segment.nodes.sortedWith(compareBy { it.x() })
        for (i in 0..sortedNodes.size - 2) {
            val node1 = sortedNodes[i]
            val node2 = sortedNodes[i + 1]
            node1.addConnection(node2)
            node2.addConnection(node1)
        }
    }
}
