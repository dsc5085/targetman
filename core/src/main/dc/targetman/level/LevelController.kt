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
import dc.targetman.character.ActionsResetter
import dc.targetman.character.CharacterActions
import dc.targetman.character.CorpseOnLimbRemoved
import dc.targetman.character.MovementSystem
import dc.targetman.character.VitalLimbsSystem
import dc.targetman.command.CommandModule
import dc.targetman.command.CommandProcessor
import dc.targetman.epf.graphics.EntityGraphDrawer
import dc.targetman.graphics.DisableDrawerExecuter
import dc.targetman.graphics.EnableDrawerExecuter
import dc.targetman.graphics.GetDrawEntities
import dc.targetman.graphics.LimbsShadowingSystem
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.EntityFinder
import dc.targetman.mechanics.InventorySystem
import dc.targetman.mechanics.PickupFactory
import dc.targetman.mechanics.ScaleSystem
import dc.targetman.mechanics.StaggerSystem
import dc.targetman.mechanics.weapon.AimOnAnimationApplied
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.mechanics.weapon.WeaponData
import dc.targetman.mechanics.weapon.WeaponSystem
import dc.targetman.physics.PhysicsUpdater
import dc.targetman.physics.PhysicsUtils
import dc.targetman.physics.collision.ForceOnCollided
import dc.targetman.physics.collision.ParticlesOnCollided
import dc.targetman.skeleton.AddLimbEntitiesOnEntityAdded
import dc.targetman.skeleton.ChangeContainerHealthOnEntityAdded
import dc.targetman.skeleton.LimbRemovedChecker
import dc.targetman.skeleton.SkeletonFactory
import dc.targetman.skeleton.SkeletonSyncSystem
import dc.targetman.util.Json
import dclib.epf.DefaultEntityManager
import dclib.epf.EntityManager
import dclib.epf.graphics.EntityDrawer
import dclib.epf.graphics.EntityDrawerManager
import dclib.epf.graphics.EntitySpriteDrawer
import dclib.epf.graphics.EntityTransformDrawer
import dclib.epf.graphics.SpriteSyncSystem
import dclib.eventing.EventDelegate
import dclib.graphics.CameraUtils
import dclib.graphics.ScreenHelper
import dclib.graphics.TextureCache
import dclib.mechanics.DamageOnCollided
import dclib.mechanics.RemoveOnCollided
import dclib.mechanics.RemoveOnNoHealthEntityAdded
import dclib.mechanics.TimedDeathSystem
import dclib.physics.AutoRotateSystem
import dclib.physics.TranslateSystem
import dclib.physics.collision.CollidedEvent
import dclib.physics.collision.CollisionChecker
import dclib.physics.particles.ParticlesManager
import dclib.system.Advancer
import dclib.system.Updater

// TODO: Put rendering related classes in a single object.
class LevelController(
        commandProcessor: CommandProcessor,
		private val textureCache: TextureCache,
		spriteBatch: PolygonSpriteBatch,
		shapeRenderer: ShapeRenderer,
		pixelsPerUnit: Float,
		private val camera: OrthographicCamera
) {
	val finished = EventDelegate<LevelFinishedEvent>()

    private val entityManager = createEntityManager()
	private val world = PhysicsUtils.createWorld()
	private val factoryTools = FactoryTools(entityManager, textureCache, world)
	private val bulletFactory = BulletFactory(factoryTools)
	private val box2DRenderer = Box2DDebugRenderer()
	private val advancer: Advancer
	private val mapRenderer: MapRenderer
	private val screenHelper = ScreenHelper(pixelsPerUnit, camera)
	private val particlesManager = ParticlesManager(textureCache, spriteBatch, screenHelper, world)
	private val entityDrawerManager = createEntityDrawerManager(spriteBatch, shapeRenderer)
	private val map = TmxMapLoader().load("maps/arena.tmx")
	private val commandModule = createCommandModule()

	init {
		advancer = createAdvancer()
		MapLoader(map, factoryTools).createObjects()
		val weaponData = Json.toObject<WeaponData>("weapons/peashooter.json")
		val pickupFactory = PickupFactory(factoryTools)
		val skeletonFactory = SkeletonFactory(textureCache)
		val weaponSkeleton = skeletonFactory.create(weaponData.skeletonPath, weaponData.atlasName)
		pickupFactory.create(Weapon(weaponData, weaponSkeleton), Vector3(0.5f, 8f, 0f))
		val scale = pixelsPerUnit / MapUtils.getPixelsPerUnit(map)
		mapRenderer = OrthogonalTiledMapRenderer(map, scale, spriteBatch)
		commandProcessor.add(commandModule)
	}

	fun dispose() {
		commandModule.dispose()
		map.dispose()
		entityManager.dispose()
		box2DRenderer.dispose()
		world.dispose()
	}

	fun update(delta: Float) {
		advancer.advance(delta)
		val player = EntityFinder.find(entityManager, Alliance.PLAYER)
		if (player != null) {
			CameraUtils.follow(player, screenHelper, camera)
		}
		if (player == null || Gdx.input.isKeyPressed(Keys.R)) {
			finished.notify(LevelFinishedEvent())
		}
	}

	fun draw() {
		mapRenderer.setView(camera)
		renderMapLayer(MapUtils.backgroundIndex)
		val entities = entityManager.getAll()
		entityDrawerManager.draw(entities)
		particlesManager.draw()
        renderMapLayer(MapUtils.getForegroundIndex(map))
//		renderBox2D()
	}

	private fun createEntityManager(): EntityManager {
		val entityManager = DefaultEntityManager()
		entityManager.entityAdded.on(RemoveOnNoHealthEntityAdded(entityManager))
		entityManager.entityAdded.on(ChangeContainerHealthOnEntityAdded(entityManager))
		entityManager.entityAdded.on(AddLimbEntitiesOnEntityAdded(entityManager))
		return entityManager
	}

	private fun createAdvancer(): Advancer {
		val collisionChecker = createCollisionChecker()
		val limbRemovedChecker = LimbRemovedChecker(entityManager)
		limbRemovedChecker.limbRemoved.on(CorpseOnLimbRemoved(entityManager, world))
		return Advancer(
				ActionsResetter(entityManager),
				createInputUpdater(),
				createAiSystem(),
				ScaleSystem(entityManager),
				AutoRotateSystem(entityManager),
				TranslateSystem(entityManager),
				PhysicsUpdater(world, entityManager),
				createSkeletonSystem(),
				collisionChecker,
				MovementSystem(entityManager, world),
				TimedDeathSystem(entityManager),
				InventorySystem(factoryTools, collisionChecker),
				WeaponSystem(entityManager, bulletFactory),
				VitalLimbsSystem(entityManager),
				StaggerSystem(factoryTools, collisionChecker),
				LimbsShadowingSystem(entityManager),
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

	private fun createCollisionChecker(): CollisionChecker {
		val collisionChecker = CollisionChecker(entityManager, world)
		val filter = getCollisionFilter()
        collisionChecker.collided.on(ForceOnCollided(entityManager, filter))
        collisionChecker.collided.on(ParticlesOnCollided(entityManager, particlesManager))
        collisionChecker.collided.on(DamageOnCollided(filter))
        collisionChecker.collided.on(RemoveOnCollided(entityManager, filter))
		return collisionChecker
	}

	private fun createEntityDrawerManager(spriteBatch: PolygonSpriteBatch, shapeRenderer: ShapeRenderer)
			: EntityDrawerManager {
		val entityDrawers = mutableListOf<EntityDrawer>()
		entityDrawers.add(EntitySpriteDrawer(spriteBatch, screenHelper, GetDrawEntities(entityManager), entityManager))
		entityDrawers.add(EntityTransformDrawer(shapeRenderer, screenHelper))
		entityDrawers.add(EntityGraphDrawer(shapeRenderer, screenHelper))
		return EntityDrawerManager(entityDrawers)
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
		if (Gdx.input.isKeyPressed(Keys.A)) {
			CharacterActions.move(player, Direction.LEFT)
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			CharacterActions.move(player, Direction.RIGHT)
		}
		if (Gdx.input.isKeyPressed(Keys.W)) {
            CharacterActions.aim(player, 1)
		} else if (Gdx.input.isKeyPressed(Keys.S)) {
            CharacterActions.aim(player, -1)
		}
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			CharacterActions.jump(player)
		}
		if (Gdx.input.isKeyPressed(Keys.J)) {
			CharacterActions.trigger(player)
		}
		if (Gdx.input.isKeyPressed(Keys.Q)) {
			CharacterActions.switchWeapon(player)
		}
		if (Gdx.input.isKeyPressed(Keys.X)) {
			CharacterActions.pickup(player)
		}
	}

	private fun createCommandModule(): CommandModule {
		val executers = listOf(
				EnableDrawerExecuter(entityDrawerManager),
				DisableDrawerExecuter(entityDrawerManager))
		return CommandModule(executers)
	}

	private fun renderMapLayer(layerIndex: Int) {
		mapRenderer.setView(camera)
		mapRenderer.render(intArrayOf(layerIndex))
	}

	private fun renderBox2D() {
		box2DRenderer.render(world, screenHelper.scaledProjectionMatrix)
	}
}