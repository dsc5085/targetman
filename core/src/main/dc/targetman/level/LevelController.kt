package dc.targetman.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.google.common.base.Predicate
import dc.targetman.AppConfig
import dc.targetman.ai.AiSystem
import dc.targetman.ai.PathUpdater
import dc.targetman.ai.Steering
import dc.targetman.audio.SoundManager
import dc.targetman.character.ActionsSystem
import dc.targetman.character.CorpseOnLimbBranchDestroyed
import dc.targetman.character.DeathForm
import dc.targetman.character.DestroyContainerOnVitalLimbDestroyed
import dc.targetman.character.MovementSystem
import dc.targetman.command.CommandModule
import dc.targetman.command.CommandProcessor
import dc.targetman.epf.graphics.GraphDrawer
import dc.targetman.graphics.DisableDrawerExecuter
import dc.targetman.graphics.EnableDrawerExecuter
import dc.targetman.graphics.GetDrawEntities
import dc.targetman.graphics.JointsDrawer
import dc.targetman.graphics.LimbsShadowingSystem
import dc.targetman.graphics.PlayerCameraOperator
import dc.targetman.level.executers.SetSpeedExecuter
import dc.targetman.level.executers.StepExecuter
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.ChangeContainerHealthOnEntityAdded
import dc.targetman.mechanics.CounterDeathSystem
import dc.targetman.mechanics.EntityFinder
import dc.targetman.mechanics.EntityUtils
import dc.targetman.mechanics.InputUpdater
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
import dclib.epf.DefaultEntityManager
import dclib.epf.EntityManager
import dclib.epf.graphics.Drawer
import dclib.epf.graphics.DrawerManager
import dclib.epf.graphics.SpriteDrawer
import dclib.epf.graphics.SpriteSyncSystem
import dclib.epf.graphics.TransformDrawer
import dclib.eventing.EventDelegate
import dclib.graphics.Render
import dclib.graphics.ScreenHelper
import dclib.graphics.TextureCache
import dclib.map.MapRenderer
import dclib.mechanics.DamageOnCollided
import dclib.mechanics.DestroyOnCollided
import dclib.mechanics.DestroyOnNoHealthEntityAdded
import dclib.mechanics.TimedDeathSystem
import dclib.particles.ParticlesManager
import dclib.physics.AutoRotateSystem
import dclib.physics.TranslateSystem
import dclib.physics.collision.CollidedEvent
import dclib.physics.collision.CollisionChecker
import dclib.system.Advancer

class LevelController(
		config: AppConfig,
        commandProcessor: CommandProcessor,
		private val textureCache: TextureCache,
		render: Render,
		private val screenHelper: ScreenHelper,
		stage: Stage
) {
	val finished = EventDelegate<LevelFinishedEvent>()

    private val entityManager = createEntityManager()
	private val world = PhysicsUtils.createWorld()
	private val factoryTools = FactoryTools(entityManager, textureCache, world)
	private val box2DRenderer = Box2DDebugRenderer()
	private val jointsDrawer = JointsDrawer(world, render.shape, screenHelper)
	private val mapRenderer: MapRenderer
	private val camera = screenHelper.viewport.camera as OrthographicCamera
	private val particlesManager = ParticlesManager(textureCache, render.sprite, screenHelper, world)
	private val map = TmxMapLoader().load("maps/arena.tmx")
	private val drawerManager: DrawerManager
	private val soundManager = SoundManager()
	private val advancer: Advancer
	private val commandModule: CommandModule

	init {
		mapRenderer = MapRenderer(map, render.sprite, screenHelper.pixelsPerUnit, camera, stage.camera)
		drawerManager = createDrawerManager(config, render, mapRenderer)
		advancer = createAdvancer()
		MapLoader(map, factoryTools).createObjects()
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
		val gameOver = EntityFinder.find(entityManager, Alliance.PLAYER) == null
		if (gameOver || Gdx.input.isKeyPressed(Keys.R)) {
			finished.notify(LevelFinishedEvent())
		}
	}

	fun draw() {
		drawerManager.draw()
	}

	private fun createEntityManager(): EntityManager {
		val entityManager = DefaultEntityManager()
		entityManager.entityDestroyed.on(DestroyContainerOnVitalLimbDestroyed(entityManager))
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
				ActionsSystem(entityManager),
				InputUpdater(entityManager, screenHelper),
				createAiSystem(collisionChecker),
				ScaleSystem(entityManager),
				AutoRotateSystem(entityManager),
				TranslateSystem(entityManager),
				PhysicsUpdater(world, entityManager, { !it.of(DeathForm.CORPSE) }),
				createSkeletonSystem(),
				collisionChecker,
				MovementSystem(entityManager, world, collisionChecker, soundManager),
				TimedDeathSystem(entityManager),
				CounterDeathSystem(entityManager),
				InventorySystem(factoryTools, collisionChecker),
				WeaponSystem(factoryTools, soundManager, particlesManager),
				StaggerSystem(factoryTools),
				LimbsShadowingSystem(entityManager),
				SpriteSyncSystem(entityManager, screenHelper),
				particlesManager,
                PlayerCameraOperator(camera, screenHelper, entityManager))
	}

	private fun createAiSystem(collisionChecker: CollisionChecker): AiSystem {
		val graphQuery = GraphQueryFactory(map, textureCache).create()
		val steering = Steering(graphQuery, world.gravity.y)
		val pathUpdater = PathUpdater(graphQuery, collisionChecker)
		return AiSystem(entityManager, steering, pathUpdater, collisionChecker, soundManager)
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
        collisionChecker.collided.on(ParticlesOnCollided(textureCache, particlesManager, mapRenderer, screenHelper))
        collisionChecker.collided.on(DamageOnCollided(filter))
        collisionChecker.collided.on(DestroyOnCollided(entityManager, filter))
		return collisionChecker
	}

	private fun createDrawerManager(
			config: AppConfig,
			render: Render,
			mapRenderer: MapRenderer
	): DrawerManager {
		val drawers = mutableListOf<Drawer>()
		drawers.add(SpriteDrawer(render.sprite, screenHelper, mapRenderer, GetDrawEntities(entityManager),
				entityManager, particlesManager))
		drawers.add(TransformDrawer(entityManager, render.shape, screenHelper))
		drawers.add(GraphDrawer(entityManager, render.shape, screenHelper))
		drawers.add(createDrawer("physics", this::renderBox2D))
		drawers.add(createDrawer("joints", jointsDrawer::draw))
		return DrawerManager(drawers, config.enabledDrawers)
	}

	private fun createDrawer(name: String, drawerFunc: () -> Unit): Drawer {
		return object : Drawer {
			override fun draw() {
				drawerFunc()
			}

			override fun getName(): String {
				return name
			}
		}
	}

	private fun getCollisionFilter(): Predicate<CollidedEvent> {
		return Predicate {
			EntityUtils.areOpposing(it!!.collision.source.entity, it.collision.target.entity)
		}
	}

	private fun createCommandModule(): CommandModule {
		val executers = listOf(
				SetSpeedExecuter(advancer),
				StepExecuter(advancer),
				EnableDrawerExecuter(drawerManager),
				DisableDrawerExecuter(drawerManager))
		return CommandModule(executers)
	}

	private fun renderBox2D() {
		box2DRenderer.render(world, screenHelper.scaledProjectionMatrix)
	}
}