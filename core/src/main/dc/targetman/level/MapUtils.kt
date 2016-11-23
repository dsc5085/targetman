package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Rectangle
import dclib.graphics.ScreenHelper

object MapUtils {
    fun createSegmentBoundsList(map: TiledMap, screenHelper: ScreenHelper): List<Rectangle> {
        val boundsList = mutableListOf<Rectangle>()
        val collisionLayer = getCollisionLayer(map)
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
        return boundsList
    }

    fun getCollisionLayer(map: TiledMap): TiledMapTileLayer {
        return map.layers.get(0) as TiledMapTileLayer
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
