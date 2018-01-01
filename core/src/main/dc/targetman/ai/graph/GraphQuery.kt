package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle

interface GraphQuery {
    fun getNearestNode(x: Float, segment: Segment): DefaultNode
    fun getNearestBelowSegment(bounds: Rectangle): Segment?
    fun getSegment(node: DefaultNode): Segment
    fun createPath(fromX: Float, fromSegment: Segment, toNode: DefaultNode): List<DefaultConnection>
}