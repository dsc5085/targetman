package dc.targetman.ai.graph

import com.badlogic.gdx.math.Rectangle

interface GraphHelper {
    fun getNearestNode(x: Float, segment: Segment): DefaultNode
    fun isBelow(node: DefaultNode, bounds: Rectangle): Boolean
    fun getNearestBelowSegment(bounds: Rectangle): Segment?
    fun getSegment(node: DefaultNode?): Segment?
    fun createPath(x: Float, startSegment: Segment, endNode: DefaultNode): List<DefaultNode>
}