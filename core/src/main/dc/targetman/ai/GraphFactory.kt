package dc.targetman.ai

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.level.MapUtils
import dclib.graphics.ScreenHelper
import java.util.*

class GraphFactory(private val map: TiledMap, private val screenHelper: ScreenHelper, private val actorSize: Vector2) {
    fun create(): DefaultIndexedGraph {
        val collisionLayer = MapUtils.getCollisionLayer(map)
        val boundsList = ArrayList<Rectangle>()
        val size = screenHelper.toWorldUnits(collisionLayer.tileWidth, collisionLayer.tileHeight)
        for (y in 0..collisionLayer.height - 1 - 1) {
            var x = 0
            while (x < collisionLayer.width) {
                val floorLength = getFloorLength(collisionLayer, x, y)
                if (floorLength > 0) {
                    val bounds = Rectangle(x.toFloat(), y.toFloat(), floorLength * size.x, size.y)
                    boundsList.add(bounds)
                    x += floorLength
                }
                x++
            }
        }
        return DefaultIndexedGraph(boundsList, actorSize)
    }

    private fun getFloorLength(layer: TiledMapTileLayer, x: Int, y: Int): Int {
        var floorLength = 0
        var i = x
        while (i < layer.width && layer.getCell(i, y) != null && layer.getCell(i, y + 1) == null) {
            floorLength++
            i++
        }
        return floorLength
    }
}