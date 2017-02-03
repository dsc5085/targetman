package dc.targetman.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.MapRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.google.common.base.Predicate
import dc.targetman.ai.AiSystem
import dc.targetman.character.CorpseOnLimbRemoved
import dc.targetman.character.MovementSystem
import dc.targetman.character.StickActions
import dc.targetman.character.VitalLimbsSystem
import dc.targetman.epf.graphics.EntityGraphDrawer
import dc.targetman.graphics.GetDrawEntities
import dc.targetman.mechanics.*
import dc.targetman.mechanics.weapon.AimOnAnimationApplied
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.mechanics.weapon.WeaponData
import dc.targetman.mechanics.weapon.WeaponSystem
import dc.targetman.physics.PhysicsUpdater
import dc.targetman.physics.PhysicsUtils
import dc.targetman.physics.collision.ForceOnCollided
import dc.targetman.physics.collision.ParticlesOnCollided
import dc.targetman.physics.collision.StainOnCollided
import dc.targetman.skeleton.ChangeContainerHealthOnEntityAdded
import dc.targetman.skeleton.LimbFactory
import dc.targetman.skeleton.LimbRemovedChecker
import dc.targetman.skeleton.SkeletonSyncSystem
import dc.targetman.util.Json
import dclib.epf.DefaultEntityManager
import dclib.epf.EntityManager
import dclib.epf.graphics.EntityDrawer
import dclib.epf.graphics.EntitySpriteDrawer
import dclib.epf.graphics.SpriteSyncSystem
import dclib.eventing.EventDelegate
import dclib.graphics.CameraUtils
import dclib.graphics.ConvexHullCache
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
import dclib.physics.collision.ContactChecker
import dclib.system.Advancer
import dclib.system.Updater

class LevelController(
		private val textureCache: TextureCache,
		spriteBatch: PolygonSpriteBatch,
		shapeRenderer: ShapeRenderer,
		pixelsPerUnit: Float,
		private val camera: OrthographicCamera
) {
	val levelFinished = EventDelegate<LevelFinishedEvent>()

	private val entityManager = createEntityManager()
	private val convexHullCache = ConvexHullCache(textureCache)
	private val world = PhysicsUtils.createWorld()
	private val entityFactory = EntityFactory(entityManager, world, convexHullCache)
	private val pickupFactory = PickupFactory(entityManager, world, textureCache)
	private val limbFactory: LimbFactory = LimbFactory(entityManager, world, textureCache)
	private val box2DRenderer = Box2DDebugRenderer()
	private val advancer: Advancer
	private val mapRenderer: MapRenderer
	private val screenHelper = ScreenHelper(pixelsPerUnit, camera)
	private val particlesManager = ParticlesManager(textureCache, spriteBatch, screenHelper, world)
	private val entityDrawers = mutableListOf<EntityDrawer>()
	private val map = TmxMapLoader().load("maps/arena.tmx")
	private var isRunning = true

	init {
		entityDrawers.add(EntitySpriteDrawer(spriteBatch, screenHelper, GetDrawEntities(entityManager), entityManager))
		entityDrawers.add(EntityGraphDrawer(shapeRenderer, screenHelper))
		advancer = createAdvancer()
		MapLoader(map, entityManager, textureCache, world, limbFactory).createObjects()
		val weaponData = Json.toObject<WeaponData>("weapons/peashooter.json")
		val atlas = textureCache.getAtlas(weaponData.atlasName)
		pickupFactory.create(Weapon(weaponData, atlas), Vector3(0.5f, 8f, 0f))
		val scale = pixelsPerUnit / MapUtils.getPixelsPerUnit(map)
		mapRenderer = OrthogonalTiledMapRenderer(map, scale, spriteBatch)
	}

	fun toggleRunning() {
		isRunning = !isRunning
	}

	fun dispose() {
		map.dispose()
		entityManager.dispose()
		box2DRenderer.dispose()
		world.dispose()
	}

	fun update(delta: Float) {
		if (isRunning) {
			advancer.advance(delta)
			val player = EntityFinder.find(entityManager, Alliance.PLAYER)
			if (player != null) {
				CameraUtils.follow(player, screenHelper, camera)
			}
			if (player == null || Gdx.input.isKeyPressed(Keys.R)) {
				levelFinished.notify(LevelFinishedEvent())
			}
		}
	}

	fun draw() {
		particlesManager.draw()
		renderEntities()
		mapRenderer.setView(camera)
		mapRenderer.render()
		renderBox2D()
	}

	private fun createEntityManager(): EntityManager {
		val entityManager = DefaultEntityManager()
		entityManager.entityAdded.on(RemoveOnNoHealthEntityAdded(entityManager))
		entityManager.entityAdded.on(ChangeContainerHealthOnEntityAdded(entityManager))
		return entityManager
	}

	private fun createAdvancer(): Advancer {
		val contactChecker = ContactChecker(world)
		val collisionChecker = createCollisionChecker(contactChecker)
		val limbRemovedChecker = LimbRemovedChecker(entityManager)
		limbRemovedChecker.limbRemoved.on(CorpseOnLimbRemoved(entityManager, world))
		return Advancer(
				createInputUpdater(),
				createAiSystem(),
				ScaleSystem(entityManager),
				AutoRotateSystem(entityManager),
				TranslateSystem(entityManager),
				PhysicsUpdater(world, entityManager),
				createSkeletonSystem(),
                collisionChecker,
				contactChecker,
				MovementSystem(entityManager, world),
				TimedDeathSystem(entityManager),
				InventorySystem(entityManager, collisionChecker, pickupFactory, limbFactory),
				WeaponSystem(entityManager, entityFactory),
				VitalLimbsSystem(entityManager),
				SpriteSyncSystem(entityManager, screenHelper),
				particlesManager)
	}

	private fun createAiSystem(): AiSystem {
		val navigator = NavigatorFactory.create(map, world, textureCache)
		return AiSystem(entityManager, navigator)
	}

	private fun createSkeletonSystem(): SkeletonSyncSystem {
		val skeletonSystem = SkeletonSyncSystem(entityManager)
		skeletonSystem.animationApplied.on(AimOnAnimationApplied())
		return skeletonSystem
	}

	private fun createCollisionChecker(contactChecker: ContactChecker): CollisionChecker {
		val collisionChecker = CollisionChecker(entityManager, contactChecker)
		val filter = getCollisionFilter()
        collisionChecker.collided.on(StainOnCollided(entityManager))
        collisionChecker.collided.on(ForceOnCollided(entityManager, filter))
        collisionChecker.collided.on(ParticlesOnCollided(particlesManager, entityFactory))
        collisionChecker.collided.on(DamageOnCollided(filter))
        collisionChecker.collided.on(RemoveOnCollided(entityManager, filter))
		return collisionChecker
	}

	private fun getCollisionFilter(): Predicate<CollidedEvent> {
		return Predicate<CollidedEvent> {
			val targetEntity = it!!.target.entity
			val targetAlliance = targetEntity.getAttribute(Alliance::class)
			val sourceAlliance = it.source.entity.getAttribute(Alliance::class)
			sourceAlliance != null && sourceAlliance.target === targetAlliance
		}
	}

	private fun createInputUpdater(): Updater {
		return Updater { processInput() }
	}

	private fun processInput() {
		val player = EntityFinder.find(entityManager, Alliance.PLAYER)
		if (player == null) {
			return
		}
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
		if (Gdx.input.isKeyJustPressed(Keys.Q)) {
			StickActions.pickup(player)
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