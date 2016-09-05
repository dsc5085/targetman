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
import com.badlogic.gdx.math.Vector3;

import dc.targetman.epf.systems.AiSystem;
import dc.targetman.epf.systems.ForceCollidedListener;
import dc.targetman.epf.systems.MovementSystem;
import dc.targetman.epf.systems.ParticlesCollidedListener;
import dc.targetman.epf.systems.RemoveCollidedListener;
import dc.targetman.epf.systems.ScaleSystem;
import dc.targetman.epf.systems.StickyCollidedListener;
import dc.targetman.epf.systems.VitalLimbsSystem;
import dc.targetman.epf.systems.WeaponSystem;
import dc.targetman.epf.util.StickActions;
import dc.targetman.level.models.Alliance;
import dclib.epf.DefaultEntityManager;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.graphics.EntityDrawer;
import dclib.epf.graphics.EntitySpriteDrawer;
import dclib.epf.systems.AutoRotateSystem;
import dclib.epf.systems.CollisionSystem;
import dclib.epf.systems.DamageCollidedListener;
import dclib.epf.systems.DrawableSystem;
import dclib.epf.systems.LimbsSystem;
import dclib.epf.systems.PhysicsSystem;
import dclib.epf.systems.RemoveOnNoHealthEntityAddedListener;
import dclib.epf.systems.TimedDeathSystem;
import dclib.epf.systems.TranslateSystem;
import dclib.geometry.UnitConverter;
import dclib.graphics.CameraUtils;
import dclib.graphics.ParticlesManager;
import dclib.graphics.TextureCache;
import dclib.system.Advancer;

public final class LevelController {

	private static final float PIXELS_PER_UNIT = 32f;

	private final EntityFactory entityFactory;
	private final EntityManager entityManager = new DefaultEntityManager();
	private final CollisionSystem collisionSystem;
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
		entityFactory = new EntityFactory(entityManager, textureCache);
		collisionSystem = createCollisionSystem();
		stickActions = new StickActions(collisionSystem);
		entityDrawers.add(new EntitySpriteDrawer(spriteBatch, camera, entityManager));
//		entityDrawers.add(new EntityTransformDrawer(shapeRenderer, camera, PIXELS_PER_UNIT));
		entityManager.addEntityAddedListener(new RemoveOnNoHealthEntityAddedListener(entityManager));
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
		processInput();
		CameraUtils.follow(targetman, unitConverter, camera);
		mapRenderer.setView(camera);
	}

	public final void draw() {
		particlesManager.draw();
		mapRenderer.render();
		List<Entity> entities = entityManager.getAll();
		for (EntityDrawer entityDrawer : entityDrawers) {
			entityDrawer.draw(entities);
		}
	}

	private CollisionSystem createCollisionSystem() {
		CollisionSystem collisionSystem = new CollisionSystem(entityManager);
		collisionSystem.addCollidedListener(new DamageCollidedListener());
		collisionSystem.addCollidedListener(new RemoveCollidedListener(entityManager));
		collisionSystem.addCollidedListener(new ForceCollidedListener(entityManager));
		collisionSystem.addCollidedListener(new StickyCollidedListener(entityManager));
		collisionSystem.addCollidedListener(new ParticlesCollidedListener(particlesManager, entityFactory));
		return collisionSystem;
	}

	private Advancer createAdvancer() {
		return new Advancer()
		.add(new AiSystem(entityManager, stickActions)) // TODO: Don't update every frame
		.add(new ScaleSystem(entityManager))
		.add(new AutoRotateSystem(entityManager))
		.add(new TranslateSystem(entityManager))
		.add(new MovementSystem(entityManager))
		.add(new LimbsSystem(entityManager))
		.add(collisionSystem)
		.add(new PhysicsSystem(-8, entityManager, collisionSystem))
		.add(new TimedDeathSystem(entityManager))
		.add(new WeaponSystem(entityManager, entityFactory))
		.add(new VitalLimbsSystem(entityManager))
		.add(new DrawableSystem(entityManager, unitConverter))
		.add(particlesManager);
	}

	private void spawnInitialEntities() {
		targetman = entityFactory.createStickman(new Vector3(1, 5, 0), Alliance.PLAYER);
		entityFactory.createStickman(new Vector3(4, 5, 0), Alliance.ENEMY);
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

}
