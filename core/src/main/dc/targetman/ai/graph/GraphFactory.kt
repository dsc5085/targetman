package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

class GraphFactory(private val boundsList: List<Rectangle>, private val actorSize: Vector2) {
    fun create(): DefaultIndexedGraph {
        return DefaultIndexedGraph(boundsList, actorSize)
    }
}