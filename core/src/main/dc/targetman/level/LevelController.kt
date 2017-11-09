package dc.targetman.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.google.common.base.Predicate
import dc.targetman.ai.AiSystem
import dc.targetman.ai.PathUpdater
import dc.targetman.ai.Steering
import dc.targetman.character.ActionsResetter
import dc.targetman.character.CharacterActions
import dc.targetman.character.CorpseOnLimbBranchDestroyed
import dc.targetman.character.DeathForm
import dc.targetman.character.MovementSystem
import dc.targetman.character.VitalLimbsSystem
import dc.targetman.command.CommandModule
import dc.targetman.command.CommandProcessor
import dc.targetman.graphics.DisableDrawerExecuter
import dc.targetman.graphics.EnableDrawerExecuter
import dc.targetman.graphics.GetDrawEntities
import dc.targetman.graphics.JointsDrawer
import dc.targetman.graphics.LimbsShadowingSystem
import dc.targetman.level.executers.SetSpeedExecuter
import dc.targetman.level.executers.StepExecuter
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.ChangeContainerHealthOnEntityAdded
import dc.targetman.mechanics.Direction
import dc.targetman.mechanics.EntityFinder
import dc.targetman.mechanics.InventorySystem
import dc.targetman.mechanics.ScaleSystem
import dc.targetman.mechanics.StaggerSystem
import dc.targetman.mechanics.weapon.AimOnAnimationApplied
import dc.targetman.mechanics.weapon.WeaponSystem
import dc.targetman.physics.PhysicsUpdater
import dc.targetman.physics.PhysicsUtils
import dc.targetman.physics.collision.ForceOnCollided
import dc.targetman.physics.collision.ParticlesOnCollided
import dc.targetman.skeleton.AddLimbEntitiesOnEntityAdded
import dc.targetman.skeleton.LimbBranchDestroyedChecker
import dc.targetman.skeleton.SkeletonSyncSystem
import dc.targetman.system.InputUtils
import dclib.epf.DefaultEntityManager
import dclib.epf.EntityManager
import dclib.epf.graphics.EntityDrawer
import dclib.epf.graphics.EntityDrawerManager
import dclib.epf.graphics.EntitySpriteDrawer
import dclib.epf.graphics.SpriteSyncSystem
import dclib.eventing.EventDelegate
import dclib.graphics.CameraUtils
import dclib.graphics.Render
import dclib.graphics.ScreenHelper
import dclib.graphics.TextureCache
import dclib.map.MapUtils
import dclib.mechanics.DamageOnCollided
import dclib.mechanics.DestroyOnCollided
import dclib.mechanics.DestroyOnNoHealthEntityAdded
import dclib.mechanics.TimedDeathSystem
import dclib.physics.AutoRotateSystem
import dclib.physics.TranslateSystem
import dclib.physics.collision.CollidedEvent
import dclib.physics.collision.CollisionChecker
import dclib.physics.particles.ParticlesManager
import dclib.system.Advancer
import dclib.system.Updater

class LevelController(
        commandProcessor: CommandProcessor,
		private val textureCache: TextureCache,
		render: Render,
		private val screenHelper: ScreenHelper
) {
	val finished = EventDelegate<LevelFinishedEvent>()

    private val entityManager = createEntityManager()
	private val world = PhysicsUtils.createWorld()
	private val factoryTools = FactoryTools(entityManager, textureCache, world)
	private val bulletFactory = BulletFactory(factoryTools)
	private val box2DRenderer = Box2DDebugRenderer()
	private val jointsDrawer = JointsDrawer(world, render.shape, screenHelper)
	private val advancer: Advancer
	private val mapRenderer: MapRenderer
	private val camera = screenHelper.viewport.camera as OrthographicCamera
	private val particlesManager = ParticlesManager(textureCache, render.sprite, screenHelper, world)
	private val map = TmxMapLoader().load("maps/simple.tmx")
	private val entityDrawerManager: EntityDrawerManager
	private val commandModule: CommandModule

	init {
		advancer = createAdvancer()
		MapLoader(map, factoryTools).createObjects()
		val scale = screenHelper.pixelsPerUnit / MapUtils.getPixelsPerUnit(map)
		mapRenderer = OrthogonalTiledMapRenderer(map, scale, render.sprite)
		entityDrawerManager = createEntityDrawerManager(render, mapRenderer)
		commandModule = createCommandModule()
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
		val entities = entityManager.getAll()
		entityDrawerManager.draw(entities)
		particlesManager.draw()
//		renderBox2D()
//        jointsDrawer.draw()
	}

	private fun createEntityManager(): EntityManager {
		val entityManager = DefaultEntityManager()
		entityManager.entityAdded.on(DestroyOnNoHealthEntityAdded(entityManager))
		entityManager.entityAdded.on(ChangeContainerHealthOnEntityAdded(entityManager))
		entityManager.entityAdded.on(AddLimbEntitiesOnEntityAdded(entityManager))
		return entityManager
	}

	private fun createAdvancer(): Advancer {
		val collisionChecker = createCollisionChecker()
		val limbBranchDestroyedChecker = LimbBranchDestroyedChecker(entityManager)
		limbBranchDestroyedChecker.destroyed.on(CorpseOnLimbBranchDestroyed(entityManager, world))
		return Advancer(
				ActionsResetter(entityManager),
				createInputUpdater(),
				createAiSystem(collisionChecker),
				ScaleSystem(entityManager),
				AutoRotateSystem(entityManager),
				TranslateSystem(entityManager),
				PhysicsUpdater(world, entityManager, { !it.of(DeathForm.CORPSE) }),
				createSkeletonSystem(),
				collisionChecker,
				MovementSystem(entityManager, world, collisionChecker),
				TimedDeathSystem(entityManager),
				InventorySystem(factoryTools, collisionChecker),
				WeaponSystem(entityManager, bulletFactory),
				VitalLimbsSystem(entityManager),
				StaggerSystem(factoryTools),
				LimbsShadowingSystem(entityManager),
				SpriteSyncSystem(entityManager, screenHelper),
				particlesManager)
	}

	private fun createAiSystem(collisionChecker: CollisionChecker): AiSystem {
		val graphQuery = GraphQueryFactory.create(map, textureCache)
		val steering = Steering(graphQuery, world.gravity.y)
		val pathUpdater = PathUpdater(graphQuery, collisionChecker)
		return AiSystem(entityManager, steering, pathUpdater)
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
        collisionChecker.collided.on(DestroyOnCollided(entityManager, filter))
		return collisionChecker
	}

	private fun createEntityDrawerManager(render: Render, mapRenderer: MapRenderer): EntityDrawerManager {
		val entityDrawers = mutableListOf<EntityDrawer>()
		entityDrawers.add(EntitySpriteDrawer(render.sprite, screenHelper, mapRenderer, camera,
                GetDrawEntities(entityManager), entityManager))
//		entityDrawers.add(EntityTransformDrawer(render.shape, screenHelper))
//		entityDrawers.add(EntityGraphDrawer(render.shape, screenHelper))
		return EntityDrawerManager(entityDrawers)
	}

	private fun getCollisionFilter(): Predicate<CollidedEvent> {
		return Predicate<CollidedEvent> {
			val targetEntity = it!!.target
			val targetAlliance = targetEntity.getAttribute(Alliance::class)
			val sourceAlliance = it.source.getAttribute(Alliance::class)
			sourceAlliance != null && sourceAlliance.target === targetAlliance
		}
	}

	private fun createInputUpdater(): Updater {
		return object : Updater {
			override fun update(delta: Float) {
				processInput()
			}
		}
	}

	private fun processInput() {
		val player = EntityFinder.find(entityManager, Alliance.PLAYER)
		if (player == null) {
			return
		}
		val cursorWorldCoords = InputUtils.getCursorWorldCoord(screenHelper)
		CharacterActions.aim(player, cursorWorldCoords)
		if (Gdx.input.isKeyPressed(Keys.A)) {
			CharacterActions.move(player, Direction.LEFT)
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			CharacterActions.move(player, Direction.RIGHT)
		}
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			CharacterActions.jump(player)
		}
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
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
				SetSpeedExecuter(advancer),
				StepExecuter(advancer),
				EnableDrawerExecuter(entityDrawerManager),
				DisableDrawerExecuter(entityDrawerManager))
		return CommandModule(executers)
	}

	private fun renderBox2D() {
		box2DRenderer.render(world, screenHelper.scaledProjectionMatrix)
	}
}