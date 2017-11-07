package dc.targetman.level

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dc.targetman.character.CharacterFactory
import dc.targetman.mechanics.Alliance
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.physics.collision.Material
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonOperations
import dclib.geometry.PolygonUtils
import dclib.geometry.toVector3
import dclib.graphics.TextureUtils
import dclib.map.MapUtils
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class MapLoader(private val map: TiledMap, private val factoryTools: FactoryTools) {
    private val foregroundLayer = map.layers[MapUtils.FOREGROUND_INDEX] as TiledMapTileLayer
    private val scale = 1 / MapUtils.getPixelsPerUnit(map)
    private val characterFactory = CharacterFactory(factoryTools)
    private val hullCache = mutableMapOf<TextureRegion, FloatArray>()

    fun createObjects() {
        createStaticObjects()
        createActors()
    }

    fun createStaticObjects() {
        val tilesVertices = mutableListOf<List<Vector2>>()
        for (x in 0..foregroundLayer.width - 1) {
            for (y in 0..foregroundLayer.height - 1) {
                val cell = foregroundLayer.getCell(x, y)
                if (cell != null) {
                    tilesVertices.add(createTileVertices(x, y, cell))
                }
            }
        }
        val unionTileVertices = PolygonOperations.union(tilesVertices)
        for (tileVertices in unionTileVertices) {
            createWall(tileVertices)
        }
    }

    fun createWall(vertices: List<Vector2>) {
        val body = Box2dUtils.createStaticBody(factoryTools.world, PolygonUtils.toFloats(vertices))
        Box2dUtils.setFilter(body, CollisionCategory.STATIC, CollisionCategory.ALL)
        val transform = Box2dTransform(body)
        val entity = Entity(TransformPart(transform))
        entity.addAttributes(Material.METAL)
        factoryTools.entityManager.add(entity)
    }

    fun createCharacter(characterPath: String, position: Vector3, alliance: Alliance): Entity {
        return characterFactory.create(characterPath, 2f, position, alliance)
    }

    private fun createTileVertices(x: Int, y: Int, cell: TiledMapTileLayer.Cell): List<Vector2> {
        var convexHull = getHull(cell.tile.textureRegion)
        PolygonUtils.scale(convexHull, scale)
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
            val characterPath = if (alliance == Alliance.PLAYER) "characters/cyborg.json" else "characters/dummy.json"
            createCharacter(characterPath, position, alliance)
        }
    }

    private fun getHull(region: TextureRegion): FloatArray {
        return hullCache.getOrPut(region, { TextureUtils.createConvexHull(region) }).copyOf()
    }
}
