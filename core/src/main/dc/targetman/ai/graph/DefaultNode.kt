package dc.targetman.ai.graph

import com.badlogic.gdx.math.Vector2
import com.google.common.base.Objects

class DefaultNode(x: Float, y: Float) {
    val position get() = _position.cpy()
    val x get() = _position.x
    val y get() = _position.y

    private val _position = Vector2(x, y)
    private val _connections = mutableSetOf<DefaultConnection>()

    fun getConnections(): Set<DefaultConnection> {
        return _connections.toSet()
    }

    fun addConnection(toNode: DefaultNode, type: ConnectionType = ConnectionType.NORMAL) {
        _connections.removeAll { it.fromNode == this && it.toNode == toNode }
        _connections.add(DefaultConnection(this, toNode, type))
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
