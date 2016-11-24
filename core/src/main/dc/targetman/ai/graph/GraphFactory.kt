package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.physics.JumpChecker
import kotlin.comparisons.compareBy

class GraphFactory(
        private val boundsList: List<Rectangle>,
        private val agentSize: Vector2,
        private val jumpChecker: JumpChecker) {
    fun create(): DefaultIndexedGraph {
        val segments = boundsList.map { Segment(it) }
        connect(segments)
        val nodes = segments.flatMap { it.nodes }
        return DefaultIndexedGraph(nodes, segments)
    }

    private fun connect(segments: List<Segment>) {
        for (i in 0..segments.size - 2) {
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
        connect(segment1.leftNode, segment2.rightNode, 0f)
        connect(segment1.rightNode, segment2.leftNode, agentSize.x)
        connect(segment2.leftNode, segment1.rightNode, 0f)
        connect(segment2.rightNode, segment1.leftNode, agentSize.x)
        connectMiddle(segment1, segment2)
        connectMiddle(segment2, segment1)
    }

    private fun connectMiddle(topSegment: Segment, bottomSegment: Segment) {
        if (topSegment.y > bottomSegment.y) {
            connectMiddle(topSegment.leftNode, bottomSegment, agentSize.x)
            connectMiddle(topSegment.rightNode, bottomSegment, 0f)
        }
    }

    private fun connectMiddle(topNode: DefaultNode, bottomSegment: Segment, localX: Float) {
        if (bottomSegment.containsX(topNode.x())) {
            val bottomNode = DefaultNode(topNode.x(), bottomSegment.y)
            bottomSegment.nodes.add(bottomNode)
            connect(topNode, bottomNode, localX)
            connect(bottomNode, topNode, localX)
        }
    }

    private fun connect(startNode: DefaultNode, endNode: DefaultNode, localX: Float) {
        val local = Vector2(localX, 0f)
        if (jumpChecker.isValid(startNode.position, endNode.position, agentSize, local)) {
            startNode.addConnection(endNode)
        }
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