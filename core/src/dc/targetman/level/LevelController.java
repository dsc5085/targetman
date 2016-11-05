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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import dc.targetman.ai.AiSystem;
import dc.targetman.ai.GraphHelper;
import dc.targetman.epf.graphics.EntityGraphDrawer;
import dc.targetman.epf.parts.MovementPart;
import dc.targetman.mechanics.Alliance;
import dc.targetman.mechanics.CorpseSystem;
import dc.targetman.mechanics.Direction;
import dc.targetman.mechanics.EntityFinder;
import dc.targetman.mechanics.MovementSystem;
import dc.targetman.mechanics.ScaleSystem;
import dc.targetman.mechanics.StickActions;
import dc.targetman.mechanics.VitalLimbsSystem;
import dc.targetman.mechanics.WeaponSystem;
import dc.targetman.physics.collision.ForceCollidedListener;
import dc.targetman.physics.collision.ParticlesCollidedListener;
import dc.targetman.physics.collision.StickyCollidedListener;
import dclib.epf.DefaultEntityManager;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntityRemovedListener;
import dclib.epf.graphics.EntityDrawer;
import dclib.epf.graphics.EntitySpriteDrawer;
import dclib.epf.graphics.SpriteSyncSystem;
import dclib.graphics.CameraUtils;
import dclib.graphics.ScreenHelper;
import dclib.graphics.TextureCache;
import dclib.mechanics.DamageCollidedListener;
import dclib.mechanics.RemoveCollidedListener;
import dclib.mechanics.RemoveOnNoHealthEntityAddedListener;
import dclib.mechanics.TimedDeathSystem;
import dclib.physics.AutoRotateSystem;
import dclib.physics.Box2dUtils;
import dclib.physics.ParticlesManager;
import dclib.physics.TranslateSystem;
import dclib.physics.collision.CollidedEvent;
import dclib.physics.collision.CollisionChecker;
import dclib.physics.limb.LimbsSystem;
import dclib.system.Advancer;
import dclib.system.Updater;

public final class LevelController {

	private static final float PIXELS_PER_UNIT = 32f;

	private boolean isRunning = true;
	private final EntityFactory entityFactory;
	private final EntityManager entityManager = new DefaultEntityManager();
	private final World world = new World(new Vector2(0, -10), true);
	private final Box2DDebugRenderer box2DRenderer = new Box2DDebugRenderer();
	private final Advancer advancer;
	private final OrthographicCamera camera;
	private final MapRenderer mapRenderer;
	private final ScreenHelper screenHelper;
	private final ParticlesManager particlesManager;
	private final List<EntityDrawer> entityDrawers = new ArrayList<EntityDrawer>();
	private final TiledMap map;

	public LevelController(final TextureCache textureCache, final PolygonSpriteBatch spriteBatch,
			final ShapeRenderer shapeRenderer) {
		camera = new OrthographicCamera(640, 480);
		map = new TmxMapLoader().load("maps/geometry.tmx");
		screenHelper = new ScreenHelper(PIXELS_PER_UNIT, camera);
		particlesManager = new ParticlesManager(textureCache, spriteBatch, screenHelper, world);
		entityFactory = new EntityFactory(entityManager, world, textureCache);
		entityDrawers.add(new EntitySpriteDrawer(spriteBatch, screenHelper, entityManager));
		entityDrawers.add(new EntityGraphDrawer(shapeRenderer, screenHelper));
		entityManager.listen(new RemoveOnNoHealthEntityAddedListener(entityManager));
		advancer = createAdvancer();
		// TODO: Need to elegantly control bodies being removed from world as last step.  This should be above createAdvancer, but unfortunately that would cause bodies to be removed prematurely and mess up the logic
		entityManager.listen(entityRemoved());
		new MapLoader(map, screenHelper, entityFactory).createObjects();
		mapRenderer = new OrthogonalTiledMapRenderer(map, 1, spriteBatch);
	}

	public final void toggleRunning() {
		isRunning = !isRunning;
	}

	public final void dispose() {
		map.dispose();
		entityManager.dispose();
	}

	public final void update(final float delta) {
		if (isRunning) {
			advancer.advance(delta);
			Entity player = EntityFinder.findPlayer(entityManager);
			CameraUtils.follow(player, screenHelper, camera);
			mapRenderer.setView(camera);
		}
	}

	public final void draw() {
		particlesManager.draw();
		mapRenderer.setView(camera);
		mapRenderer.render();
		renderEntities();
		renderBox2D();
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
		// TODO: Calculate actor size
		GraphHelper graphHelper = new GraphHelper(map, screenHelper, new Vector2(1, 2));
		return new Advancer(
				createInputUpdater(),
				new AiSystem(entityManager, graphHelper),
				new ScaleSystem(entityManager),
				new AutoRotateSystem(entityManager),
				new TranslateSystem(entityManager),
				createPhysicsUpdater(),
				createCollisionChecker(),
				new MovementSystem(entityManager, world),
				new LimbsSystem(entityManager),
				new TimedDeathSystem(entityManager),
				new WeaponSystem(entityManager, entityFactory),
				new VitalLimbsSystem(entityManager),
				new CorpseSystem(entityManager),
				new SpriteSyncSystem(entityManager, screenHelper),
				particlesManager);
	}

	private CollisionChecker createCollisionChecker() {
		CollisionChecker collisionSystem = new CollisionChecker(world);
		Predicate<CollidedEvent> filter = getCollisionFilter();
		collisionSystem.listen(new DamageCollidedListener(filter));
		collisionSystem.listen(new StickyCollidedListener(entityManager));
		collisionSystem.listen(new ForceCollidedListener(entityManager, filter));
		collisionSystem.listen(new ParticlesCollidedListener(particlesManager, entityFactory));
		collisionSystem.listen(new RemoveCollidedListener(entityManager, filter));
		return collisionSystem;
	}

	private Predicate<CollidedEvent> getCollisionFilter() {
		return new Predicate<CollidedEvent>() {
			@Override
			public boolean apply(final CollidedEvent event) {
				Entity targetEntity = event.getTarget().getEntity();
				Alliance targetAlliance = getAlliance(targetEntity);
				Alliance sourceAlliance = getAlliance(event.getSource().getEntity());
				return sourceAlliance != null && sourceAlliance.getTarget() == targetAlliance
						&& !targetEntity.has(MovementPart.class);
			}
		};
	}

	private Alliance getAlliance(final Entity entity) {
		Iterable<Alliance> alliances = Iterables.filter(entity.getAttributes(), Alliance.class);
		return Iterables.getFirst(alliances, null);
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
				world.step(delta, 8, 3);
			}
		};
	}

	private void processInput() {
		Entity player = EntityFinder.findPlayer(entityManager);
		Direction moveDirection = Direction.NONE;
		if (Gdx.input.isKeyPressed(Keys.A)) {
			moveDirection = Direction.LEFT;
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			moveDirection = Direction.RIGHT;
		}
		StickActions.move(player, moveDirection);
		int aimDirection = 0;
		if (Gdx.input.isKeyPressed(Keys.W)){
			aimDirection = 1;
		} else if (Gdx.input.isKeyPressed(Keys.S)) {
			aimDirection = -1;
		}
		StickActions.aim(player, aimDirection);
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			StickActions.jump(player);
		}
		if (Gdx.input.isKeyPressed(Keys.J)){
			StickActions.trigger(player);
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
