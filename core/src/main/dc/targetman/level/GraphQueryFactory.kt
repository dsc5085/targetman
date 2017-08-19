package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
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

object GraphQueryFactory {
    fun create(map: TiledMap, textureCache: TextureCache): GraphQuery {
        val segmentBoundsList = MapUtils.createSegmentBoundsList(map)
        val staticWorld = PhysicsUtils.createWorld()
        val entityManager = DefaultEntityManager()
        val factoryTools = FactoryTools(entityManager, textureCache, staticWorld)
        val mapLoader = MapLoader(map, factoryTools)
        // TODO: Creating an entity just for this is wasteful.
        val aiEntity = mapLoader.createCharacter("characters/dummy.json", Vector3(), Alliance.ENEMY)
        mapLoader.createStaticObjects()
        val agentSize = aiEntity[TransformPart::class].transform.size
        val jumpChecker = JumpChecker(staticWorld, aiEntity[MovementPart::class].speed)
        val graph = GraphFactory(segmentBoundsList, agentSize, jumpChecker).create()
        entityManager.dispose()
        staticWorld.dispose()
        return DefaultGraphQuery(graph)
    }
}