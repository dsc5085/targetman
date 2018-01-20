package dc.targetman.ai.graph

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph
import com.badlogic.gdx.utils.Array
import dc.targetman.util.ArrayUtils

class DefaultIndexedGraph(private val nodes: List<DefaultNode>, private val segments: List<Segment>)
    : IndexedGraph<DefaultNode> {
    override fun getConnections(fromNode: DefaultNode): Array<Connection<DefaultNode>> {
        return ArrayUtils.toGdxArray(fromNode.getConnections())
    }

    override fun getNodeCount(): Int {
        return nodes.size
    }

    override fun getIndex(node: DefaultNode): Int {
        return nodes.indexOf(node)
    }

    fun getSegments(): List<Segment> {
        return segments.toList()
    }
}
