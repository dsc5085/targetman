package dc.targetman.level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dclib.epf.DefaultEntityManager;
import dclib.epf.DefaultEntitySystemManager;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystemManager;
import dclib.epf.graphics.EntityDrawer;
import dclib.epf.graphics.EntitySpriteDrawer;
import dclib.epf.parts.TranslatePart;
import dclib.epf.systems.DrawableSystem;
import dclib.epf.systems.LimbsSystem;
import dclib.epf.systems.PhysicsSystem;
import dclib.epf.systems.TranslateSystem;
import dclib.geometry.UnitConverter;
import dclib.graphics.CameraUtils;
import dclib.graphics.TextureCache;
import dclib.physics.BodyCollidedListener;
import dclib.system.Advancer;

public final class LevelController {

	private static final float PIXELS_PER_UNIT = 32f;

	private final EntityFactory entityFactory;
	private final EntityManager entityManager = new DefaultEntityManager();
	private final EntitySystemManager entitySystemManager;
	private final Advancer advancer;
	private final Camera camera;
	private final UnitConverter unitConverter;
	private final List<EntityDrawer> entityDrawers = new ArrayList<EntityDrawer>();
	private final Set<Entity> groundedEntities = new HashSet<Entity>();
	private Entity targetman;

	public LevelController(final TextureCache textureCache, final PolygonSpriteBatch spriteBatch) {
		camera = new OrthographicCamera(1024, 768);
		unitConverter = new UnitConverter(PIXELS_PER_UNIT, camera);
		entityFactory = new EntityFactory(textureCache);
		entitySystemManager = createEntitySystemManager();
		entityDrawers.add(new EntitySpriteDrawer(spriteBatch, camera));
		spawnInitialEntities();
		advancer = createAdvancer();
	}

	public final void dispose() {
		entityManager.dispose();
	}

	public final void update(final float delta) {
		groundedEntities.clear();
		advancer.advance(delta);
		processInput();
		CameraUtils.follow(targetman, unitConverter, camera);
	}

	public final void draw() {
		List<Entity> entities = entityManager.getAll();
		for (EntityDrawer entityDrawer : entityDrawers) {
			entityDrawer.draw(entities);
		}
	}

	private EntitySystemManager createEntitySystemManager() {
		EntitySystemManager entitySystemManager = new DefaultEntitySystemManager(entityManager);
		entitySystemManager.add(new LimbsSystem());
		entitySystemManager.add(new TranslateSystem());
		PhysicsSystem physicsSystem = new PhysicsSystem(entityManager, -8);
		physicsSystem.addBodyCollidedListener(bodyCollided());
		entitySystemManager.add(physicsSystem);
		entitySystemManager.add(new DrawableSystem(unitConverter));
		return entitySystemManager;
	}

	private BodyCollidedListener bodyCollided() {
		return new BodyCollidedListener() {
			@Override
			public void collided(final Entity entity, final Vector2 offset) {
				if (offset.y > 0) {
					groundedEntities.add(entity);
				}
			}
		};
	}

	private void spawnInitialEntities() {
		Entity wall = entityFactory.createWall(new Vector2(2f, 3), new Vector3(-2, -2, 0));
		entityManager.add(wall);
		Entity wall2 = entityFactory.createWall(new Vector2(3, 0.05f), new Vector3(0, -2, 0));
		entityManager.add(wall2);
		Entity wall3 = entityFactory.createWall(new Vector2(3, 0.05f), new Vector3(4, -2, 0));
		entityManager.add(wall3);
		List<Entity> targetmanEntities = entityFactory.createTargetman(new Vector2(1, 1), new Vector3(1, 0, 0));
		targetman = targetmanEntities.get(0);
		entityManager.addAll(targetmanEntities);
	}

	private Advancer createAdvancer() {
		return new Advancer() {
			@Override
			protected void update(final float delta) {
				entitySystemManager.update(delta);
			}
		};
	}

	private void processInput() {
		final float speed = 3;
		TranslatePart translatePart = targetman.get(TranslatePart.class);
		float velocityX = 0;
		if (Gdx.input.isKeyPressed(Keys.A)) {
			velocityX = -speed;
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			velocityX = speed;
		}
		translatePart.setVelocityX(velocityX);
		final float jumpSpeed = 4;
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			if (groundedEntities.contains(targetman)) {
				translatePart.setVelocityY(jumpSpeed);
			}
		}
	}

}
