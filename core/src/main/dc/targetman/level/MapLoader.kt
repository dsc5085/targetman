package dc.targetman.level

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dc.targetman.character.CharacterFactory
import dc.targetman.mechanics.Alliance
import dc.targetman.physics.Interactivity
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.physics.collision.Material
import dclib.epf.Entity
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonOperations
import dclib.geometry.PolygonUtils
import dclib.geometry.toVector3
import dclib.graphics.TextureUtils
import dclib.map.Cell
import dclib.map.MapUtils
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class MapLoader(private val map: TiledMap, private val factoryTools: FactoryTools) {
    private val scale = 1 / MapUtils.getPixelsPerUnit(map)
    private val characterFactory = CharacterFactory(factoryTools)
    private val hullCache = mutableMapOf<TextureRegion, FloatArray>()

    fun createObjects() {
        createWalls()
        createActors()
        createLadders()
    }

    fun createWalls() {
        val layer = map.layers[MapUtils.FOREGROUND_INDEX] as TiledMapTileLayer
        val tilesVertices = mutableListOf<List<Vector2>>()
        for (cell in MapUtils.getCells(layer)) {
            tilesVertices.add(createTileVertices(cell))
        }
        val unionTileVertices = PolygonOperations.union(tilesVertices)
        for (tileVertices in unionTileVertices) {
            createWall(tileVertices)
        }
    }

    fun createCharacter(characterPath: String, position: Vector3, alliance: Alliance): Entity {
        return characterFactory.create(characterPath, 2f, position, alliance)
    }

    private fun createTileVertices(cell: Cell): List<Vector2> {
        val tileCell = cell.cell
        var convexHull = getHull(tileCell.tile.textureRegion)
        PolygonUtils.scale(convexHull, scale)
        if (tileCell.flipHorizontally) {
            convexHull = PolygonUtils.flipX(convexHull)
        }
        if (tileCell.flipVertically) {
            convexHull = PolygonUtils.flipY(convexHull)
        }
        convexHull = PolygonUtils.shift(convexHull, cell.point.x.toFloat(), cell.point.y.toFloat())
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

    private fun createWall(vertices: List<Vector2>) {
        val body = Box2dUtils.createStaticBody(factoryTools.world, PolygonUtils.toFloats(vertices))
        Box2dUtils.setFilter(body, CollisionCategory.STATIC, CollisionCategory.ALL)
        val transform = Box2dTransform(body)
        val entity = Entity(TransformPart(transform))
        entity.addAttributes(Material.METAL)
        factoryTools.entityManager.add(entity)
    }

    private fun createLadders() {
        val layer = map.layers[MapUtils.BACKGROUND_INDEX] as TiledMapTileLayer
        val tilesVertices = mutableListOf<List<Vector2>>()
        for (cell in MapUtils.getCells(layer)) {
            if (cell.cell.tile.properties.containsKey(Interactivity.CLIMB.toString())) {
                tilesVertices.add(createTileVertices(cell))
            }
        }
        val unionTileVertices = PolygonOperations.union(tilesVertices)
        for (tileVertices in unionTileVertices) {
            createLadder(tileVertices)
        }
    }

    private fun createLadder(vertices: List<Vector2>) {
        val body = Box2dUtils.createStaticBody(factoryTools.world, PolygonUtils.toFloats(vertices))
        Box2dUtils.setFilter(body, null, CollisionCategory.BOUNDS)
        Box2dUtils.setSensor(body, true)
        val transform = Box2dTransform(body)
        val entity = Entity(TransformPart(transform))
        entity.addAttributes(Interactivity.CLIMB)
        factoryTools.entityManager.add(entity)
    }

    private fun getHull(region: TextureRegion): FloatArray {
        return hullCache.getOrPut(region, { TextureUtils.createConvexHull(region) }).copyOf()
    }
}
