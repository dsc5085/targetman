package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.ai.MoveAi
import dc.targetman.ai.Steering
import dc.targetman.ai.graph.DefaultGraphQuery
import dc.targetman.ai.graph.GraphFactory
import dc.targetman.epf.parts.MovementPart
import dc.targetman.mechanics.Alliance
import dc.targetman.physics.JumpChecker
import dc.targetman.physics.JumpVelocitySolver
import dc.targetman.physics.PhysicsUtils
import dclib.epf.DefaultEntityManager
import dclib.epf.parts.TransformPart
import dclib.graphics.TextureCache

object MoveAiFactory {
    fun create(
            map: TiledMap,
            world: World,
            textureCache: TextureCache
    ): MoveAi {
        val segmentBoundsList = MapUtils.createSegmentBoundsList(map)
        val staticWorld = PhysicsUtils.createWorld()
        val entityManager = DefaultEntityManager()
        val factoryTools = FactoryTools(entityManager, textureCache, staticWorld)
        val mapLoader = MapLoader(map, factoryTools)
        // TODO: Creating an entity just for this is wasteful.
        val aiEntity = mapLoader.createCharacter("characters/dummy.json", Vector3(), Alliance.ENEMY)
        mapLoader.createStaticObjects()
        val agentSize = aiEntity[TransformPart::class].transform.size
        val movementPart = aiEntity[MovementPart::class]
        val speed = Vector2(movementPart.moveSpeed, movementPart.jumpSpeed)
        val jumpVelocitySolver = JumpVelocitySolver(speed, world.gravity.y)
        val jumpChecker = JumpChecker(staticWorld, jumpVelocitySolver)
        val graph = GraphFactory(segmentBoundsList, agentSize, jumpChecker).create()
        val graphQuery = DefaultGraphQuery(graph)
        val steering = Steering(graphQuery, jumpVelocitySolver)
        entityManager.dispose()
        staticWorld.dispose()
        return MoveAi(graphQuery, steering, world)
    }
}