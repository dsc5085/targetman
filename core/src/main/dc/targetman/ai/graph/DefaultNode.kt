package dc.targetman.ai.graph

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.math.Vector2
import com.google.common.base.Objects
import java.util.*

class DefaultNode(x: Float, y: Float) {
    private val connectedNodes = HashSet<DefaultNode>()

    private val _position = Vector2(x, y)
    val position: Vector2
        get() = _position.cpy()

    val x: Float
        get() = position.x

    val y: Float
        get() = position.y

    val connections: List<Connection<DefaultNode>>
        get() = connectedNodes.map { DefaultConnection(this, it) }

    fun addConnection(endNode: DefaultNode) {
        connectedNodes.add(endNode)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(position)
    }

    override fun equals(other: Any?): Boolean {
        return other is DefaultNode && other.position == position
    }

    override fun toString(): String {
        return position.toString()
    }
}
