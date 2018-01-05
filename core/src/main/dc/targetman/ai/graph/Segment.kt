package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle
import dclib.geometry.right
import dclib.geometry.top

class Segment(val bounds: Rectangle) {
    val leftNode = DefaultNode(bounds.x, y)
    val rightNode = DefaultNode(bounds.right, y)
    val left get() = leftNode.x
    val right get() = rightNode.x
    val y get() = bounds.top

    private val nodes: MutableSet<DefaultNode>

    init {
        nodes = mutableSetOf(leftNode, rightNode)
    }

    fun getNodes(): Set<DefaultNode> {
        return nodes.toSet()
    }

    fun getOrCreateNode(nodeX: Float): DefaultNode {
        return nodes.firstOrNull { it.x == nodeX } ?: createNode(nodeX)
    }

    fun createNode(nodeX: Float): DefaultNode {
        val node = DefaultNode(nodeX, y)
        nodes.add(node)
        return node
    }

    fun containsX(x: Float): Boolean {
        return x in left..right
    }

    fun overlapsX(bounds: Rectangle): Boolean {
        return left in bounds.x..bounds.right
                || right in bounds.x..bounds.right
                || bounds.x in left..right
                || bounds.right in left..right
    }

    override fun toString(): String {
        return bounds.toString()
    }
}
