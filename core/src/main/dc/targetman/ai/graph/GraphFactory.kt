package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.physics.JumpChecker
import dclib.physics.Box2dUtils
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
            connectNodesOnSameSegment(segment)
        }
    }

    private fun connect(segment1: Segment, segment2: Segment) {
        connectJump(segment1.leftNode, segment2.rightNode, 0f)
        connectJump(segment1.rightNode, segment2.leftNode, agentSize.x)
        connectJump(segment2.leftNode, segment1.rightNode, 0f)
        connectJump(segment2.rightNode, segment1.leftNode, agentSize.x)
        connectMiddle(segment1, segment2)
        connectMiddle(segment2, segment1)
    }

    private fun connectMiddle(topSegment: Segment, bottomSegment: Segment) {
        if (topSegment.y > bottomSegment.y) {
            val edgeBuffer = agentSize.x + Box2dUtils.ROUNDING_ERROR
            connectMiddle(topSegment, topSegment.leftNode, bottomSegment, -edgeBuffer, 0f)
            connectMiddle(topSegment, topSegment.rightNode, bottomSegment, edgeBuffer, agentSize.x)
        }
    }

    private fun connectMiddle(topSegment: Segment, topNode: DefaultNode, bottomSegment: Segment, landingOffsetX: Float,
                              localX: Float) {
        val bottomX = topNode.x + landingOffsetX
        if (bottomSegment.containsX(bottomX)) {
            val cornerNode = DefaultNode(bottomX, topNode.y)
            topNode.addConnection(cornerNode)
            cornerNode.addConnection(topNode)
            val bottomNode = DefaultNode(bottomX, bottomSegment.y)
            connectJump(cornerNode, bottomNode, localX)
            connectJump(bottomNode, cornerNode, localX)
            // TODO: cornerNode shouldn't be part of top segment.  it is hanging in midair
            topSegment.nodes.add(cornerNode)
            bottomSegment.nodes.add(bottomNode)
        }
    }

    private fun connectJump(startNode: DefaultNode, endNode: DefaultNode, localX: Float) {
        val local = Vector2(localX, 0f)
        if (jumpChecker.isValid(startNode.position, endNode.position, agentSize, local)) {
            startNode.addConnection(endNode)
        }
    }

    private fun connectNodesOnSameSegment(segment: Segment) {
        val sortedNodes = segment.nodes.sortedWith(compareBy { it.x })
        for (i in 0..sortedNodes.size - 2) {
            val node1 = sortedNodes[i]
            val node2 = sortedNodes[i + 1]
            node1.addConnection(node2)
            node2.addConnection(node1)
        }
    }
}