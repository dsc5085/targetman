package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Rectangle

object MapUtils {
    val backgroundIndex = 0

    fun getForegroundIndex(map: TiledMap): Int {
        return map.layers.count - 1
    }

    fun getPixelsPerUnit(map: TiledMap): Float {
        val layer = map.layers.first() as TiledMapTileLayer
        return Math.max(layer.tileWidth, layer.tileHeight)
    }

    fun createSegmentBoundsList(map: TiledMap): List<Rectangle> {
        val boundsList = mutableListOf<Rectangle>()
        val foregroundLayer = map.layers[getForegroundIndex(map)] as TiledMapTileLayer
        for (y in 0..foregroundLayer.height - 1 - 1) {
            var x = 0
            while (x < foregroundLayer.width) {
                val floorLength = getFloorLength(foregroundLayer, x, y)
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
