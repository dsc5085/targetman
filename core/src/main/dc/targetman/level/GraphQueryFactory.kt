package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import dc.targetman.ai.graph.DefaultGraphQuery
import dc.targetman.ai.graph.GraphFactory
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.epf.parts.MovementPart
import dc.targetman.mechanics.Alliance
import dc.targetman.physics.JumpChecker
import dc.targetman.physics.PhysicsUtils
import dclib.epf.DefaultEntityManager
import dclib.epf.parts.TransformPart
import dclib.graphics.TextureCache
import dclib.map.MapUtils

object GraphQueryFactory {
    fun create(map: TiledMap, textureCache: TextureCache): GraphQuery {
        val segmentBoundsList = createSegmentBoundsList(map)
        val staticWorld = PhysicsUtils.createWorld()
        val entityManager = DefaultEntityManager()
        val factoryTools = FactoryTools(entityManager, textureCache, staticWorld)
        val mapLoader = MapLoader(map, factoryTools)
        // TODO: Creating an entity just for this is wasteful.
        val aiEntity = mapLoader.createCharacter("characters/dummy.json", Vector3(), Alliance.ENEMY)
        mapLoader.createWalls()
        val agentSize = aiEntity[TransformPart::class].transform.size
        val jumpChecker = JumpChecker(staticWorld, aiEntity[MovementPart::class].speed)
        val graph = GraphFactory(segmentBoundsList, agentSize, jumpChecker).create()
        entityManager.dispose()
        staticWorld.dispose()
        return DefaultGraphQuery(graph)
    }

    fun createSegmentBoundsList(map: TiledMap): List<Rectangle> {
        val boundsList = mutableListOf<Rectangle>()
        val foregroundLayer = MapUtils.getForegroundLayer(map)
        for (y in 0 until foregroundLayer.height - 1 - 1) {
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