package dc.targetman.ai.graph

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dc.targetman.epf.parts.MovementPart
import dc.targetman.level.EntityFactory
import dc.targetman.level.MapLoader
import dc.targetman.level.MapUtils
import dc.targetman.mechanics.Alliance
import dc.targetman.physics.JumpChecker
import dc.targetman.physics.JumpVelocitySolver
import dc.targetman.physics.PhysicsUtils
import dclib.epf.DefaultEntityManager
import dclib.epf.parts.TransformPart
import dclib.graphics.ScreenHelper
import dclib.graphics.TextureCache

object GraphQueryFactory {
    fun create(map: TiledMap, screenHelper: ScreenHelper, textureCache: TextureCache): GraphQuery {
        val world = PhysicsUtils.createWorld()
        val entityManager = DefaultEntityManager()
        val entityFactory = EntityFactory(entityManager, world, textureCache)
        MapLoader(map, screenHelper, entityFactory).createStaticObjects()
        // TODO: Creating an entity just for this is wasteful.
        val aiEntity = entityFactory.createStickman(Vector3(), Alliance.ENEMY)
        val movementPart = aiEntity[MovementPart::class.java]
        val jumpVelocitySolver = JumpVelocitySolver(Vector2(movementPart.moveSpeed, movementPart.jumpSpeed))
        val size = aiEntity[TransformPart::class.java].transform.size
        val jumpChecker = JumpChecker(world, jumpVelocitySolver)
        val segmentBoundsList = MapUtils.createSegmentBoundsList(map, screenHelper)
        val graph = GraphFactory(segmentBoundsList, size, jumpChecker).create()
        entityManager.dispose()
        world.dispose()
        return DefaultGraphQuery(graph)
    }
}