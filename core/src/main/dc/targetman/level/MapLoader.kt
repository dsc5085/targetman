package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.geometry.PolygonOperations
import dc.targetman.mechanics.Alliance
import dclib.geometry.PolygonUtils
import dclib.geometry.toVector3
import dclib.graphics.ScreenHelper

class MapLoader(
        private val map: TiledMap,
        private val screenHelper: ScreenHelper,
        private val entityFactory: EntityFactory
) {
    fun createObjects() {
        createStaticObjects()
        createActors()
    }

    fun createStaticObjects() {
        val tilesVertices = mutableListOf<List<Vector2>>()
        val collisionLayer = MapUtils.getCollisionLayer(map)
        for (x in 0..collisionLayer.width - 1) {
            for (y in 0..collisionLayer.height - 1) {
                if (collisionLayer.getCell(x, y) != null) {
                    tilesVertices.add(createTileVertices(x, y))
                }
            }
        }
        val unionTileVertices = PolygonOperations.union(tilesVertices)
        for (tileVertices in unionTileVertices) {
            entityFactory.createWall(tileVertices)
        }
    }

    private fun createTileVertices(x: Int, y: Int): List<Vector2> {
        val bounds = Rectangle(x.toFloat(), y.toFloat(), 1f, 1f)
        val vertices = PolygonUtils.createRectangleVertices(bounds)
        return PolygonUtils.toVectors(vertices)
    }

    private fun createActors() {
        val layer = map.layers.get(1)
        for (mapObject in layer.objects.getByType(TiledMapTileMapObject::class.java)) {
            val position = screenHelper.toWorldUnits(mapObject.x, mapObject.y).toVector3()
            val allianceString = mapObject.tile.properties.get(Alliance::class.java.simpleName, String::class.java)
            val alliance = Alliance.valueOf(allianceString)
            val characterPath = if (alliance == Alliance.PLAYER) "characters/cyborg.json" else "characters/thug.json"
            entityFactory.createCharacter(characterPath, position, alliance)
        }
    }
}
