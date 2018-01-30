package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dc.targetman.ai.graph.DefaultGraphQuery
import dc.targetman.ai.graph.GraphFactory
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.epf.parts.MovementPart
import dc.targetman.mechanics.Alliance
import dc.targetman.physics.Interactivity
import dc.targetman.physics.JumpChecker
import dc.targetman.physics.PhysicsUtils
import dclib.epf.DefaultEntityManager
import dclib.epf.parts.TransformPart
import dclib.geometry.div
import dclib.graphics.TextureCache
import dclib.map.MapUtils

class GraphQueryFactory(private val map: TiledMap, private val textureCache: TextureCache) {
    fun create(): GraphQuery {
        val staticWorld = PhysicsUtils.createWorld()
        val entityManager = DefaultEntityManager()
        val factoryTools = FactoryTools(entityManager, textureCache, staticWorld)
        val mapLoader = MapLoader(map, factoryTools)
        // TODO: Creating an entity just for this is wasteful.
        val aiEntity = mapLoader.createCharacter("characters/dummy.json", Vector3(), Alliance.ENEMY)
        val agentSize = aiEntity[TransformPart::class].transform.size
        val segmentBoundsList = createSegmentBoundsList(agentSize)
        val ladderBoundsList = createLadderBoundsList()
        mapLoader.createWalls()
        val jumpChecker = JumpChecker(staticWorld, aiEntity[MovementPart::class].maxSpeed)
        val graph = GraphFactory(segmentBoundsList, ladderBoundsList, agentSize, jumpChecker).create()
        entityManager.dispose()
        staticWorld.dispose()
        return DefaultGraphQuery(graph)
    }

    private fun createSegmentBoundsList(agentSize: Vector2): List<Rectangle> {
        val boundsList = mutableListOf<Rectangle>()
        val foregroundLayer = MapUtils.getForegroundLayer(map)
        for (y in 0 until foregroundLayer.height - 1) {
            var x = 0
            while (x < foregroundLayer.width) {
                val floorLength = getFloorLength(foregroundLayer, agentSize, x, y)
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

    private fun getFloorLength(layer: TiledMapTileLayer, agentSize: Vector2, x: Int, y: Int): Int {
        var maxX = x
        while (maxX < layer.width && layer.getCell(maxX, y) != null && hasEnoughSpace(layer, agentSize, maxX, y + 1)) {
            maxX++
        }
        return maxX - x
    }

    private fun hasEnoughSpace(layer: TiledMapTileLayer, agentSize: Vector2, x: Int, y: Int): Boolean {
        val numTilesToCheck = Math.min(MathUtils.ceil(agentSize.y), layer.height - y - 1)
        for (i in 0 until numTilesToCheck) {
            if (layer.getCell(x, y + i) != null) {
                return false
            }
        }
        return true
    }

    private fun createLadderBoundsList(): List<Rectangle> {
        // This is simply the createSegmentBoundsList method rotated 90 degrees onto the y-axis
        val boundsList = mutableListOf<Rectangle>()
        val backgroundLayers = MapUtils.getBackgroundLayers(map)
        for (backgroundLayer in backgroundLayers) {
            for (x in 0 until backgroundLayer.width - 1) {
                var y = 0
                while (y < backgroundLayer.height) {
                    val ladderHeight = getLadderHeight(backgroundLayer, x, y)
                    if (ladderHeight > 0) {
                        // TODO: Assumes ladder width is only 1f
                        val bounds = Rectangle(x.toFloat(), y.toFloat(), 1f, ladderHeight.toFloat())
                        boundsList.add(bounds)
                        y += ladderHeight
                    }
                    y++
                }
            }
        }
        return boundsList
    }

    private fun getLadderHeight(layer: TiledMapTileLayer, x: Int, y: Int): Int {
        var i = y
        val pixelsPerUnit = MapUtils.getPixelsPerUnit(layer)
        do {
            val cell = layer.getCell(x, i)
            if (cell == null || !cell.tile.properties.containsKey(Interactivity.CLIMB.name)) {
                break
            }
            val tileSize = getTileSize(cell, pixelsPerUnit)
            i += MathUtils.ceil(tileSize.y)
        } while (i < layer.height)
        return i - y
    }

    private fun getTileSize(cell: TiledMapTileLayer.Cell, pixelsPerUnit: Float): Vector2 {
        val width = cell.tile.textureRegion.regionWidth.toFloat()
        val height = cell.tile.textureRegion.regionHeight.toFloat()
        return Vector2(width, height).div(pixelsPerUnit)
    }
}