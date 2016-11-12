package dc.targetman.epf.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import dc.targetman.epf.parts.AiPart
import dclib.epf.Entity
import dclib.epf.graphics.EntityDrawer
import dclib.epf.parts.TransformPart
import dclib.geometry.base
import dclib.graphics.ScreenHelper

class EntityGraphDrawer(private val shapeRenderer: ShapeRenderer, private val screenHelper: ScreenHelper)
    : EntityDrawer {
    override fun draw(entities: List<Entity>) {
        screenHelper.setScaledProjectionMatrix(shapeRenderer)
        shapeRenderer.color = Color.CYAN
        shapeRenderer.begin(ShapeType.Line)
        for (entity in entities) {
            draw(entity)
        }
        shapeRenderer.end()
    }

    private fun draw(entity: Entity) {
        val aiPart = entity.tryGet(AiPart::class.java)
        if (aiPart != null && !aiPart.path.isEmpty()) {
            val path = aiPart.path.map { it.position }
            val bounds = entity.get(TransformPart::class.java).transform.bounds
            var start = bounds.base
            var end = path[0]
            for (i in 1..path.size - 1) {
                shapeRenderer.line(start, end)
                start = path[i - 1]
                end = path[i]
            }
        }
    }
}
