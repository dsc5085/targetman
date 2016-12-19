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
        val pointSize = 0.2f
        val aiPart = entity.tryGet(AiPart::class)
        if (aiPart != null && aiPart.path.isNotEmpty()) {
            val bounds = entity[TransformPart::class].transform.bounds
            val points = listOf(bounds.base) + aiPart.path.map { it.position }
            for (i in 0..points.size - 2) {
                val start = points[i]
                val end = points[i + 1]
                shapeRenderer.rect(end.x - pointSize / 2, end.y - pointSize / 2, pointSize, pointSize)
                shapeRenderer.line(start, end)
            }
        }
    }
}
