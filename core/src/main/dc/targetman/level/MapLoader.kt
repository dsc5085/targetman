package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.character.CharacterFactory
import dc.targetman.geometry.PolygonOperations
import dc.targetman.mechanics.Alliance
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.physics.collision.Material
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.toVector3
import dclib.graphics.TextureCache
import dclib.graphics.TextureUtils
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class MapLoader(
        private val map: TiledMap,
        private val entityManager: EntityManager,
        textureCache: TextureCache,
        private val world: World
) {
    private val collisionLayer = MapUtils.getCollisionLayer(map)
    // TODO: Use screenhelper to encapsulate unit conversion logic
    private val scale = 1 / MapUtils.getPixelsPerUnit(map)
    private val characterFactory = CharacterFactory(textureCache, world)

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
            createWall(tileVertices)
        }
    }

    fun createWall(vertices: List<Vector2>) {
        val entity = Entity()
        val body = Box2dUtils.createStaticBody(world, PolygonUtils.toFloats(vertices))
        Box2dUtils.setFilter(body, CollisionCategory.STATIC, CollisionCategory.ALL)
        val transform = Box2dTransform(body)
        entity.attach(TransformPart(transform))
        entity.addAttributes(Material.METAL)
        entityManager.add(entity)
    }

    fun createCharacter(characterPath: String, position: Vector3, alliance: Alliance): Entity {
        val entity = characterFactory.create(characterPath, 2f, position, alliance)
        entityManager.add(entity)
        return entity
    }

    private fun createTileVertices(x: Int, y: Int, cell: TiledMapTileLayer.Cell): List<Vector2> {
        // TODO: Cache convex hull for performance
        var convexHull = TextureUtils.createConvexHull(cell.tile.textureRegion)
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
            val characterPath = if (alliance == Alliance.PLAYER) "characters/cyborg.json" else "characters/thug.json"
            createCharacter(characterPath, position, alliance)
        }
    }
}
