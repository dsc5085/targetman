package dc.targetman.level;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

import dc.targetman.ai.AiSystem;
import dc.targetman.epf.util.StickActions;
import dc.targetman.gamelogic.MovementSystem;
import dc.targetman.gamelogic.ScaleSystem;
import dc.targetman.gamelogic.VitalLimbsSystem;
import dc.targetman.gamelogic.WeaponSystem;
import dc.targetman.level.models.Alliance;
import dc.targetman.physics.collision.ForceCollidedListener;
import dc.targetman.physics.collision.ParticlesCollidedListener;
import dc.targetman.physics.collision.RemoveCollidedListener;
import dc.targetman.physics.collision.StickyCollidedListener;
import dclib.epf.DefaultEntityManager;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntityRemovedListener;
import dclib.epf.graphics.EntityDrawer;
import dclib.epf.graphics.EntitySpriteDrawer;
import dclib.epf.graphics.SpriteSyncSystem;
import dclib.gamelogic.DamageCollidedListener;
import dclib.gamelogic.RemoveOnNoHealthEntityAddedListener;
import dclib.gamelogic.TimedDeathSystem;
import dclib.geometry.UnitConverter;
import dclib.graphics.CameraUtils;
import dclib.graphics.TextureCache;
import dclib.physics.AutoRotateSystem;
import dclib.physics.Box2dUtils;
import dclib.physics.ParticlesManager;
import dclib.physics.TranslateSystem;
import dclib.physics.collision.CollisionChecker;
import dclib.physics.limb.LimbsSystem;
import dclib.system.Advancer;
import dclib.system.Updater;

public final class LevelController {

	private static final float PIXELS_PER_UNIT = 32f;

	private final EntityFactory entityFactory;
	private final EntityManager entityManager = new DefaultEntityManager();
	private final World world = new World(new Vector2(0, -10), true);
	private final Box2DDebugRenderer box2DRenderer = new Box2DDebugRenderer();
	private final StickActions stickActions;
	private final Advancer advancer;
	private final OrthographicCamera camera;
	private final MapRenderer mapRenderer;
	private final UnitConverter unitConverter;
	private final ParticlesManager particlesManager;
	private final List<EntityDrawer> entityDrawers = new ArrayList<EntityDrawer>();
	private final TiledMap map;
	private Entity targetman;

	public LevelController(final TextureCache textureCache, final PolygonSpriteBatch spriteBatch,
			final ShapeRenderer shapeRenderer) {
		camera = new OrthographicCamera(640, 480);
		unitConverter = new UnitConverter(PIXELS_PER_UNIT, camera);
		particlesManager = new ParticlesManager(textureCache, camera, spriteBatch, unitConverter);
		entityFactory = new EntityFactory(entityManager, world, textureCache);
		stickActions = new StickActions(world);
		entityDrawers.add(new EntitySpriteDrawer(spriteBatch, camera, entityManager));
//		entityDrawers.add(new EntityTransformDrawer(shapeRenderer, camera, PIXELS_PER_UNIT));
		entityManager.listen(new RemoveOnNoHealthEntityAddedListener(entityManager));
		entityManager.listen(entityRemoved());
		advancer = createAdvancer();
		map = new TmxMapLoader().load("maps/test_level.tmx");
		MapUtils.spawn(map, entityFactory);
		mapRenderer = new OrthogonalTiledMapRenderer(map, 1, spriteBatch);
		spawnInitialEntities();
	}

	public final void dispose() {
		map.dispose();
		entityManager.dispose();
	}

	public final void update(final float delta) {
		advancer.advance(delta);
		if (targetman.isActive()) {
			CameraUtils.follow(targetman, unitConverter, camera);
		}
		mapRenderer.setView(camera);
	}

	public final void draw() {
		particlesManager.draw();
		mapRenderer.render();
		renderEntities();
//		renderBox2D();
	}

	private EntityRemovedListener entityRemoved() {
		return new EntityRemovedListener() {
			@Override
			public void removed(final Entity entity) {
				Body body = Box2dUtils.findBody(world, entity);
				if (body != null) {
					world.destroyBody(body);
				}
			}
		};
	}

	private Advancer createAdvancer() {
		return new Advancer(
				createInputUpdater(),
				new AiSystem(entityManager, stickActions),  // TODO: Don't update every frame
				new ScaleSystem(entityManager),
				new AutoRotateSystem(entityManager),
				new TranslateSystem(entityManager),
				createPhysicsUpdater(),
				createCollisionChecker(),
				new MovementSystem(entityManager),
				new LimbsSystem(entityManager),
				new TimedDeathSystem(entityManager),
				new WeaponSystem(entityManager, entityFactory),
				new VitalLimbsSystem(entityManager),
				new SpriteSyncSystem(entityManager, unitConverter),
				particlesManager);
	}

	private CollisionChecker createCollisionChecker() {
		CollisionChecker collisionSystem = new CollisionChecker(entityManager, world);
		collisionSystem.listen(new DamageCollidedListener());
		collisionSystem.listen(new StickyCollidedListener(entityManager));
		collisionSystem.listen(new ForceCollidedListener(entityManager));
		collisionSystem.listen(new ParticlesCollidedListener(particlesManager, entityFactory));
		collisionSystem.listen(new RemoveCollidedListener(entityManager));
		return collisionSystem;
	}

	private Updater createInputUpdater() {
		return new Updater() {
			@Override
			public void update(final float delta) {
				processInput();
			}
		};
	}

	private Updater createPhysicsUpdater() {
		return new Updater() {
			@Override
			public void update(final float delta) {
				world.step(delta, 1, 1);
			}
		};
	}

	private void spawnInitialEntities() {
		targetman = entityFactory.createStickman(new Vector3(1, 5, 0), Alliance.PLAYER);
		entityFactory.createStickman(new Vector3(20, 20, 0), Alliance.ENEMY);
	}

	private void processInput() {
		float moveDirection = 0;
		if (Gdx.input.isKeyPressed(Keys.A)) {
			moveDirection = -1;
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			moveDirection = 1;
		}
		stickActions.move(targetman, moveDirection);
		float aimDirection = 0;
		if (Gdx.input.isKeyPressed(Keys.W)){
			aimDirection = 1;
		} else if (Gdx.input.isKeyPressed(Keys.S)) {
			aimDirection = -1;
		}
		stickActions.aim(targetman, aimDirection);
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			stickActions.jump(targetman);
		}
		if (Gdx.input.isKeyPressed(Keys.J)){
			stickActions.trigger(targetman);
		}
	}

	private void renderEntities() {
		List<Entity> entities = entityManager.getAll();
		for (EntityDrawer entityDrawer : entityDrawers) {
			entityDrawer.draw(entities);
		}
	}

	private void renderBox2D() {
		Matrix4 renderMatrix = new Matrix4(camera.combined);
		renderMatrix.scale(PIXELS_PER_UNIT, PIXELS_PER_UNIT, 1);
		box2DRenderer.render(world, renderMatrix);
	}

}
