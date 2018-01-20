package dc.targetman.ai

import dc.targetman.ai.graph.DefaultConnection

/**
 * Keeps track of the current path for an AI, including its current connection.
 */
class Path {
    val isNotEmpty get() = _connections.isNotEmpty()
    val currentConnection get() = _connections.first()
    val connections get() = _connections.toList()

    private val _connections = mutableListOf<DefaultConnection>()

    fun set(newPath: List<DefaultConnection>) {
        _connections.clear()
        _connections.addAll(newPath)
    }

    fun pop() {
        _connections.removeAt(0)
    }
}