package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle

interface GraphHelper {
    fun getNearestNode(x: Float, segment: Segment): DefaultNode
    fun isBelow(node: DefaultNode?, bounds: Rectangle, belowSegment: Segment): Boolean
    fun getNearestBelowSegment(bounds: Rectangle): Segment?
    fun getSegment(node: DefaultNode?): Segment?
    fun createPath(x: Float, startSegment: Segment, endNode: DefaultNode): List<DefaultNode>
}