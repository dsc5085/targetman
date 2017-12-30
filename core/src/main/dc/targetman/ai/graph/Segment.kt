package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle
import dclib.geometry.right
import dclib.geometry.top
import dclib.util.CollectionUtils

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

    fun getOrAdd(node: DefaultNode): DefaultNode {
        return CollectionUtils.getOrAdd(nodes, node)
    }

    fun add(node: DefaultNode) {
        nodes.add(node)
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
