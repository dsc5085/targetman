package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.physics.JumpChecker
import dclib.geometry.center
import dclib.geometry.grow
import dclib.physics.Box2dUtils
import dclib.util.Maths

class GraphFactory(
        private val walls: List<Rectangle>,
        private val ladders: List<Rectangle>,
        private val agentSize: Vector2,
        private val jumpChecker: JumpChecker
) {
    fun create(): DefaultIndexedGraph {
        val segments = walls.map { Segment(it) }
        connect(segments)
        val nodes = segments.flatMap { it.getNodes() }
        return DefaultIndexedGraph(nodes, segments)
    }

    private fun connect(segments: List<Segment>) {
        createNormalConnections(segments)
        createLadderConnections(segments)
        for (segment in segments) {
            connectNodesOnSameSegment(segment)
        }
    }

    private fun createNormalConnections(segments: List<Segment>) {
        for (i in 0 until segments.size - 1) {
            val segment1 = segments[i]
            for (j in i + 1 until segments.size) {
                val segment2 = segments[j]
                connectNormal(segment1, segment2)
            }
        }
    }

    private fun connectNormal(segment1: Segment, segment2: Segment) {
        val leftToRightDistance = Maths.distance(segment1.left, segment2.right)
        val rightToLeftDistance = Maths.distance(segment1.right, segment2.left)
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
            connectMiddle(topSegment.leftNode, bottomSegment)
            connectMiddle(topSegment.rightNode, bottomSegment)
        }
    }

    private fun connectMiddle(topNode: DefaultNode, bottomSegment: Segment) {
        if (bottomSegment.containsX(topNode.x)) {
            val bottomNode = bottomSegment.getOrCreateNode(topNode.x)
            connectJump(topNode, bottomNode, Box2dUtils.ROUNDING_ERROR)
            connectJump(bottomNode, topNode, Box2dUtils.ROUNDING_ERROR)
        }
    }

    private fun connectJump(fromNode: DefaultNode, toNode: DefaultNode, edgeBuffer: Float = 0f) {
        val localLeft = Vector2(-edgeBuffer, 0f)
        val localRight = Vector2(agentSize.x + edgeBuffer, 0f)
        if (jumpChecker.isValid(fromNode.position, toNode.position, agentSize, localLeft)
                || jumpChecker.isValid(fromNode.position, toNode.position, agentSize, localRight)) {
            fromNode.addConnection(toNode)
        }
    }

    private fun createLadderConnections(segments: List<Segment>) {
        for (ladder in ladders) {
            // Add extra buffer space to check for ladder-node collisions
            val ladderCheckBounds = ladder.grow(ladder.width / 2, 0f)
            val ladderNodes = mutableListOf<DefaultNode>()
            for (segment in segments) {
                if (ladderCheckBounds.contains(segment.leftNode.position)) {
                    ladderNodes.add(segment.leftNode)
                } else if (ladderCheckBounds.contains(segment.rightNode.position)) {
                    ladderNodes.add(segment.rightNode)
                } else if (ladder.y - segment.y <= 0 && segment.overlapsX(ladder)) {
                    ladderNodes.add(segment.createNode(ladder.center.x))
                }
            }
            for (i in 0 until ladderNodes.size - 1) {
                val ladderNode1 = ladderNodes[i]
                for (j in i + 1 until ladderNodes.size) {
                    val ladderNode2 = ladderNodes[j]
                    ladderNode1.addConnection(ladderNode2, ConnectionType.CLIMB)
                    ladderNode2.addConnection(ladderNode1, ConnectionType.CLIMB)
                }
            }
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