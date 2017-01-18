package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Rectangle

object MapUtils {
    fun getCollisionLayer(map: TiledMap): TiledMapTileLayer {
        return map.layers.get(0) as TiledMapTileLayer
    }

    fun getPixelsPerUnit(map: TiledMap): Float {
        val collisionLayer = getCollisionLayer(map)
        return Math.max(collisionLayer.tileWidth, collisionLayer.tileHeight)
    }

    fun createSegmentBoundsList(map: TiledMap): List<Rectangle> {
        val boundsList = mutableListOf<Rectangle>()
        val collisionLayer = getCollisionLayer(map)
        for (y in 0..collisionLayer.height - 1 - 1) {
            var x = 0
            while (x < collisionLayer.width) {
                val floorLength = getFloorLength(collisionLayer, x, y)
                if (floorLength > 0) {
                    val bounds = Rectangle(x.toFloat(), y.toFloat(), floorLength.toFloat(), 1f)
                    boundsList.add(bounds)
                    x += floorLength
                }
                x++
            }
        }
        return boundsList
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
