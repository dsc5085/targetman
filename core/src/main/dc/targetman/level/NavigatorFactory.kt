package dc.targetman.level

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.World
import dc.targetman.ai.Navigator
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
import dclib.graphics.ScreenHelper
import dclib.graphics.TextureCache

object NavigatorFactory {
    fun create(
            map: TiledMap,
            world: World,
            screenHelper: ScreenHelper,
            textureCache: TextureCache): Navigator {
        val segmentBoundsList = MapUtils.createSegmentBoundsList(map, screenHelper)
        val staticWorld = PhysicsUtils.createWorld()
        val entityManager = DefaultEntityManager()
        val entityFactory = EntityFactory(entityManager, staticWorld, textureCache)
        // TODO: Creating an entity just for this is wasteful.
        val aiEntity = entityFactory.createCharacter("characters/thug.json", Vector3(), Alliance.ENEMY)
        MapLoader(map, entityFactory).createStaticObjects()
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
        return Navigator(graphQuery, steering, world)
    }
}