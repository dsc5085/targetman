package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle
import dclib.geometry.right
import dclib.geometry.top
import dclib.util.CollectionUtils
import dclib.util.Maths

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

    fun containsX(x: Float): Boolean {
        return Maths.between(x, left, right)
    }

    fun overlapsX(bounds: Rectangle): Boolean {
        return Maths.between(left, bounds.x, bounds.right)
                || Maths.between(right, bounds.x, bounds.right)
                || Maths.between(bounds.x, left, right)
                || Maths.between(bounds.right, left, right)
    }
}
