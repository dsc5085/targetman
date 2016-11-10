package dc.targetman.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.MapRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.google.common.base.Predicate
import dc.targetman.ai.AiSystem
import dc.targetman.ai.GraphHelper
import dc.targetman.epf.graphics.EntityGraphDrawer
import dc.targetman.mechanics.*
import dc.targetman.mechanics.weapon.WeaponSystem
import dc.targetman.physics.PhysicsUpdater
import dc.targetman.physics.collision.ForceOnCollided
import dc.targetman.physics.collision.ParticlesOnCollided
import dc.targetman.physics.collision.StickOnCollided
import dclib.epf.DefaultEntityManager
import dclib.epf.Entity
import dclib.epf.graphics.EntityDrawer
import dclib.epf.graphics.EntitySpriteDrawer
import dclib.epf.graphics.SpriteSyncSystem
import dclib.graphics.CameraUtils
import dclib.graphics.ScreenHelper
import dclib.graphics.TextureCache
import dclib.mechanics.DamageOnCollided
import dclib.mechanics.RemoveOnCollided
import dclib.mechanics.RemoveOnNoHealthEntityAdded
import dclib.mechanics.TimedDeathSystem
import dclib.physics.AutoRotateSystem
import dclib.physics.ParticlesManager
import dclib.physics.TranslateSystem
import dclib.physics.collision.CollidedEvent
import dclib.physics.collision.CollisionChecker
import dclib.physics.limb.LimbsSystem
import dclib.system.Advancer
import dclib.system.Updater
import java.util.*

class LevelController(textureCache: TextureCache, spriteBatch: PolygonSpriteBatch, shapeRenderer: ShapeRenderer) {
	companion object {
		private val PIXELS_PER_UNIT = 32f
	}
	
	private var isRunning = true
	private val entityFactory: EntityFactory
	private val entityManager = DefaultEntityManager()
	private val world = World(Vector2(0f, -10f), true)
	private val box2DRenderer = Box2DDebugRenderer()
	private val advancer: Advancer
    private val camera = OrthographicCamera(640f, 480f)
	private val mapRenderer: MapRenderer
	private val screenHelper: ScreenHelper
	private val particlesManager: ParticlesManager
	private val entityDrawers = ArrayList<EntityDrawer>()
    private val map = TmxMapLoader().load("maps/geometry.tmx")

	init {
		screenHelper = ScreenHelper(PIXELS_PER_UNIT, camera)
		particlesManager = ParticlesManager(textureCache, spriteBatch, screenHelper, world)
		entityFactory = EntityFactory(entityManager, world, textureCache)
		entityDrawers.add(EntitySpriteDrawer(spriteBatch, screenHelper, entityManager))
		entityDrawers.add(EntityGraphDrawer(shapeRenderer, screenHelper))
		entityManager.entityAdded.on(RemoveOnNoHealthEntityAdded(entityManager))
		advancer = createAdvancer()
		MapLoader(map, screenHelper, entityFactory).createObjects()
		mapRenderer = OrthogonalTiledMapRenderer(map, 1f, spriteBatch)
	}

	fun toggleRunning() {
		isRunning = !isRunning
	}

	fun dispose() {
		map.dispose()
		entityManager.dispose()
	}

	fun update(delta: Float) {
		if (isRunning) {
			advancer.advance(delta)
			val player = EntityFinder.findPlayer(entityManager)
			CameraUtils.follow(player, screenHelper, camera)
			mapRenderer.setView(camera)
		}
	}

	fun draw() {
		particlesManager.draw()
		mapRenderer.setView(camera)
		mapRenderer.render()
		renderEntities()
//		renderBox2D()
	}

	private fun createAdvancer(): Advancer {
// TODO: Calculate actor size
		val graphHelper = GraphHelper(map, screenHelper, Vector2(1f, 2f))
		val limbsSystem = LimbsSystem(entityManager)
		limbsSystem.limbRemoved.on(CorpseOnLimbRemoved(entityManager))
		return Advancer(
				createInputUpdater(),
				AiSystem(entityManager, graphHelper),
				ScaleSystem(entityManager),
				AutoRotateSystem(entityManager),
				TranslateSystem(entityManager),
                PhysicsUpdater(world, entityManager),
				createCollisionChecker(),
				MovementSystem(entityManager, world),
                BoundsSyncSystem(entityManager),
				limbsSystem,
				TimedDeathSystem(entityManager),
                WeaponSystem(entityManager, entityFactory),
				VitalLimbsSystem(entityManager),
				SpriteSyncSystem(entityManager, screenHelper),
				particlesManager)
	}

	private fun createCollisionChecker(): CollisionChecker {
		val collisionSystem = CollisionChecker(entityManager, world)
		val filter = getCollisionFilter()
		collisionSystem.collided.on(StickOnCollided(entityManager))
		collisionSystem.collided.on(ForceOnCollided(entityManager, filter))
        collisionSystem.collided.on(ParticlesOnCollided(particlesManager, entityFactory))
        collisionSystem.collided.on(DamageOnCollided(filter))
		collisionSystem.collided.on(RemoveOnCollided(entityManager, filter))
		return collisionSystem
	}

	private fun getCollisionFilter(): Predicate<CollidedEvent> {
		return object : Predicate<CollidedEvent> {
			// TODO: How to make this a non-nullable event
			override fun apply(event: CollidedEvent?): Boolean {
				val targetEntity = event!!.target.entity
				val targetAlliance = getAlliance(targetEntity)
				val sourceAlliance = getAlliance(event.source.entity)
				return sourceAlliance != null && sourceAlliance.target === targetAlliance
			}
		}
	}

	private fun getAlliance(entity: Entity): Alliance? {
		return entity.attributes.firstOrNull { it is Alliance } as Alliance?
	}

	private fun createInputUpdater(): Updater {
		return object : Updater {
			override fun update(delta: Float) {
				processInput()
			}
		}
	}

	private fun processInput() {
		val player = EntityFinder.findPlayer(entityManager)
		var moveDirection = Direction.NONE
		if (Gdx.input.isKeyPressed(Keys.A)) {
			moveDirection = Direction.LEFT
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			moveDirection = Direction.RIGHT
		}
		StickActions.move(player, moveDirection)
		var aimDirection = 0
		if (Gdx.input.isKeyPressed(Keys.W)) {
			aimDirection = 1
		} else if (Gdx.input.isKeyPressed(Keys.S)) {
			aimDirection = -1
		}
		StickActions.aim(player, aimDirection)
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			StickActions.jump(player)
		}
		if (Gdx.input.isKeyPressed(Keys.J)) {
			StickActions.trigger(player)
		}
	}

	private fun renderEntities() {
		val entities = entityManager.all
		for (entityDrawer in entityDrawers) {
			entityDrawer.draw(entities)
		}
	}

	private fun renderBox2D() {
		box2DRenderer.render(world, screenHelper.scaledProjectionMatrix)
	}
}