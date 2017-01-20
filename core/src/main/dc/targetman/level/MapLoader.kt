package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Vector2
import dc.targetman.geometry.PolygonOperations
import dc.targetman.mechanics.Alliance
import dclib.geometry.PolygonUtils
import dclib.geometry.toVector3
import dclib.graphics.TextureUtils

class MapLoader(private val map: TiledMap, private val entityFactory: EntityFactory) {
    private val collisionLayer = MapUtils.getCollisionLayer(map)
    // TODO: Use screenhelper to encapsulate unit conversion logic
    private val scale = 1 / MapUtils.getPixelsPerUnit(map)

    fun createObjects() {
        createStaticObjects()
        createActors()
    }

    fun createStaticObjects() {
        val tilesVertices = mutableListOf<List<Vector2>>()
        for (x in 0..collisionLayer.width - 1) {
            for (y in 0..collisionLayer.height - 1) {
                val cell = collisionLayer.getCell(x, y)
                if (cell != null) {
                    tilesVertices.add(createTileVertices(x, y, cell))
                }
            }
        }
        val unionTileVertices = PolygonOperations.union(tilesVertices)
        for (tileVertices in unionTileVertices) {
            entityFactory.createWall(tileVertices)
        }
    }

    private fun createTileVertices(x: Int, y: Int, cell: TiledMapTileLayer.Cell): List<Vector2> {
        // TODO: Cache convex hull for performance
        var convexHull = TextureUtils.createConvexHull(cell.tile.textureRegion)
        convexHull = PolygonUtils.scale(convexHull, scale)
        if (cell.flipHorizontally) {
            convexHull = PolygonUtils.flipX(convexHull)
        }
        if (cell.flipVertically) {
            convexHull = PolygonUtils.flipY(convexHull)
        }
        convexHull = PolygonUtils.shift(convexHull, x.toFloat(), y.toFloat())
        return PolygonUtils.toVectors(convexHull)
    }

    private fun createActors() {
        val layer = map.layers.get(1)
        for (mapObject in layer.objects.getByType(TiledMapTileMapObject::class.java)) {
            val position = Vector2(mapObject.x, mapObject.y).scl(scale).toVector3()
            val allianceString = mapObject.tile.properties.get(Alliance::class.java.simpleName, String::class.java)
            val alliance = Alliance.valueOf(allianceString)
            val characterPath = if (alliance == Alliance.PLAYER) "characters/cyborg.json" else "characters/thug.json"
            entityFactory.createCharacter(characterPath, position, alliance)
        }
    }
}
