package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.epf.parts.AiPart
import dclib.epf.Entity
import dclib.epf.parts.TransformPart

internal class Agent(val entity: Entity, val targetBounds: Rectangle, graphHelper: GraphHelper) {
    val profile = entity.get(AiPart::class.java).profile
    val bounds = entity.get(TransformPart::class.java).transform.bounds
    val belowSegment = graphHelper.getNearestBelowSegment(bounds)
    val path = entity.get(AiPart::class.java).path
    val nextNode = if (path.isEmpty()) null else path[0]
}
