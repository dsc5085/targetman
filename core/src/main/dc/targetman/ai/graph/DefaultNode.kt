package dc.targetman.ai.graph

import com.badlogic.gdx.math.Vector2
import com.google.common.base.Objects

class DefaultNode(x: Float, y: Float) {
    private val _position = Vector2(x, y)
    val position get() = _position.cpy()

    val x get() = position.x
    val y get() = position.y
    val connections = mutableListOf<DefaultConnection>()

    fun addConnection(endNode: DefaultNode, type: ConnectionType = ConnectionType.NORMAL) {
        connections.add(DefaultConnection(this, endNode, type))
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
