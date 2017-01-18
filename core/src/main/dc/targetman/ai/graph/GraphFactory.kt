package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.physics.JumpChecker
import dclib.physics.Box2dUtils
import dclib.util.Maths
import kotlin.comparisons.compareBy

class GraphFactory(
        private val boundsList: List<Rectangle>,
        private val agentSize: Vector2,
        private val jumpChecker: JumpChecker
) {
    fun create(): DefaultIndexedGraph {
        val segments = boundsList.map { Segment(it) }
        connect(segments)
        val nodes = segments.flatMap { it.getNodes() }
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
        val leftToRightDistance = Maths.distance(segment1.left, segment2.right)
        val rightToLeftDistance = Maths.distance(segment1.right, segment1.left)
        val minDistance = Math.min(leftToRightDistance, rightToLeftDistance)
        if (minDistance > 0) {
            if (leftToRightDistance == minDistance) {
                connectJump(segment1.leftNode, segment2.rightNode)
                connectJump(segment2.rightNode, segment1.leftNode)
            } else {
                connectJump(segment1.rightNode, segment2.leftNode)
                connectJump(segment2.leftNode, segment1.rightNode)
            }
        }
        connectMiddle(segment1, segment2)
        connectMiddle(segment2, segment1)
    }

    private fun connectMiddle(topSegment: Segment, bottomSegment: Segment) {
        if (topSegment.y > bottomSegment.y) {
            val edgeBuffer = agentSize.x + Box2dUtils.ROUNDING_ERROR
            connectMiddle(topSegment, topSegment.leftNode, bottomSegment, -edgeBuffer)
            connectMiddle(topSegment, topSegment.rightNode, bottomSegment, edgeBuffer)
        }
    }

    private fun connectMiddle(topSegment: Segment, topNode: DefaultNode, bottomSegment: Segment,
                              landingOffsetX: Float) {
        val bottomX = topNode.x + landingOffsetX
        if (bottomSegment.containsX(bottomX)) {
            // TODO: cornerNode shouldn't be part of top segment.  it is hanging in midair
            // TODO: Should probably also add corner node between regular jump nodes
            val cornerNode = topSegment.getOrAdd(DefaultNode(bottomX, topNode.y))
            topNode.addConnection(cornerNode)
            cornerNode.addConnection(topNode)
            val bottomNode = bottomSegment.getOrAdd(DefaultNode(bottomX, bottomSegment.y))
            connectJump(cornerNode, bottomNode)
            connectJump(bottomNode, cornerNode)
        }
    }

    private fun connectJump(startNode: DefaultNode, endNode: DefaultNode) {
        val localLeft = Vector2(0f, 0f)
        val localRight = Vector2(agentSize.x, 0f)
        if (jumpChecker.isValid(startNode.position, endNode.position, agentSize, localLeft)
                || jumpChecker.isValid(startNode.position, endNode.position, agentSize, localRight)) {
            startNode.addConnection(endNode)
        }
    }

    private fun connectNodesOnSameSegment(segment: Segment) {
        val sortedNodes = segment.getNodes().sortedWith(compareBy { it.x })
        for (i in 0..sortedNodes.size - 2) {
            val node1 = sortedNodes[i]
            val node2 = sortedNodes[i + 1]
            node1.addConnection(node2)
            node2.addConnection(node1)
        }
    }
}