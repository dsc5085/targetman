package dc.targetman.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.MapRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.google.common.base.Predicate
import dc.targetman.ai.AiSystem
import dc.targetman.epf.graphics.EntityGraphDrawer
import dc.targetman.mechanics.*
import dc.targetman.mechanics.weapon.AimOnAnimationApplied
import dc.targetman.mechanics.weapon.WeaponSystem
import dc.targetman.physics.PhysicsUpdater
import dc.targetman.physics.PhysicsUtils
import dc.targetman.physics.collision.ForceOnCollided
import dc.targetman.physics.collision.ParticlesOnCollided
import dc.targetman.physics.collision.StickOnCollided
import dc.targetman.skeleton.AddLimbsOnEntityAdded
import dc.targetman.skeleton.LimbRemovedChecker
import dc.targetman.skeleton.SkeletonSystem
import dclib.epf.DefaultEntityManager
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
import dclib.physics.collision.ContactChecker
import dclib.physics.collision.EntityCollisionChecker
import dclib.system.Advancer
import dclib.system.Updater

class LevelController(
        private val textureCache: TextureCache,
        spriteBatch: PolygonSpriteBatch,
        shapeRenderer: ShapeRenderer) {
	private val PIXELS_PER_UNIT = 32f

	private val entityFactory: EntityFactory
	private val entityManager = DefaultEntityManager()
    private val world = PhysicsUtils.createWorld()
	private val box2DRenderer = Box2DDebugRenderer()
	private val advancer: Advancer
    private val camera = OrthographicCamera(640f, 480f)
	private val mapRenderer: MapRenderer
	private val screenHelper: ScreenHelper
	private val particlesManager: ParticlesManager
	private val entityDrawers = mutableListOf<EntityDrawer>()
	private val map = TmxMapLoader().load("maps/test_level.tmx")
	private var isRunning = true

	init {
		screenHelper = ScreenHelper(PIXELS_PER_UNIT, camera)
		particlesManager = ParticlesManager(textureCache, spriteBatch, screenHelper, world)
        entityFactory = EntityFactory(PIXELS_PER_UNIT, entityManager, world, textureCache)
		entityDrawers.add(EntitySpriteDrawer(spriteBatch, screenHelper, entityManager))
		entityDrawers.add(EntityGraphDrawer(shapeRenderer, screenHelper))
		entityManager.entityAdded.on(RemoveOnNoHealthEntityAdded(entityManager))
        entityManager.entityAdded.on(AddLimbsOnEntityAdded(entityManager))
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
        world.dispose()
	}

	fun update(delta: Float) {
		if (isRunning) {
			advancer.advance(delta)
			val player = EntityFinder.findPlayer(entityManager)
            if (player != null) {
                CameraUtils.follow(player, screenHelper, camera)
            }
			mapRenderer.setView(camera)
		}
	}

	fun draw() {
		particlesManager.draw()
		mapRenderer.setView(camera)
		mapRenderer.render()
		renderEntities()
        renderBox2D()
	}

	private fun createAdvancer(): Advancer {
        val limbRemovedChecker = LimbRemovedChecker(entityManager)
        limbRemovedChecker.limbRemoved.on(CorpseOnLimbRemoved(entityManager))
		return Advancer(
				createInputUpdater(),
				createAiSystem(),
				ScaleSystem(entityManager),
				AutoRotateSystem(entityManager),
				TranslateSystem(entityManager),
                PhysicsUpdater(world, entityManager),
                createSkeletonSystem(),
				createContactChecker(),
				MovementSystem(entityManager, world),
				TimedDeathSystem(entityManager),
                WeaponSystem(entityManager, entityFactory),
				VitalLimbsSystem(entityManager),
				SpriteSyncSystem(entityManager, screenHelper),
				particlesManager)
	}

	private fun createAiSystem(): AiSystem {
        val navigator = NavigatorFactory.create(map, world, screenHelper, textureCache)
        return AiSystem(entityManager, navigator)
	}

    private fun createSkeletonSystem(): SkeletonSystem {
        val skeletonSystem = SkeletonSystem(entityManager)
        skeletonSystem.animationApplied.on(AimOnAnimationApplied())
        return skeletonSystem
    }

	private fun createContactChecker(): ContactChecker {
		val contactChecker = ContactChecker(world)
		val entityCollisionChecker = EntityCollisionChecker(contactChecker)
		val filter = getCollisionFilter()
		entityCollisionChecker.collided.on(StickOnCollided(entityManager))
		entityCollisionChecker.collided.on(ForceOnCollided(entityManager, filter))
		entityCollisionChecker.collided.on(ParticlesOnCollided(particlesManager, entityFactory))
		entityCollisionChecker.collided.on(DamageOnCollided(filter))
		entityCollisionChecker.collided.on(RemoveOnCollided(entityManager, filter))
		return contactChecker
	}

	private fun getCollisionFilter(): Predicate<CollidedEvent> {
        return Predicate<CollidedEvent> {
            val targetEntity = it!!.target.entity
            val targetAlliance = EntityUtils.getAlliance(targetEntity)
            val sourceAlliance = EntityUtils.getAlliance(it.source.entity)
            sourceAlliance != null && sourceAlliance.target === targetAlliance
		}
	}

	private fun createInputUpdater(): Updater {
        return Updater { processInput() }
	}

	private fun processInput() {
		val player = EntityFinder.findPlayer(entityManager)
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