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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dclib.epf.DefaultEntityManager;
import dclib.epf.DefaultEntitySystemManager;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystemManager;
import dclib.epf.graphics.EntityDrawer;
import dclib.epf.graphics.EntitySpriteDrawer;
import dclib.epf.graphics.EntityTransformDrawer;
import dclib.epf.parts.LimbAnimationsPart;
import dclib.epf.parts.LimbsPart;
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

	public LevelController(final TextureCache textureCache, final PolygonSpriteBatch spriteBatch,
			final ShapeRenderer shapeRenderer) {
		camera = new OrthographicCamera(320, 240);
		unitConverter = new UnitConverter(PIXELS_PER_UNIT, camera);
		entityFactory = new EntityFactory(textureCache);
		entitySystemManager = createEntitySystemManager();
		entityDrawers.add(new EntitySpriteDrawer(spriteBatch, camera));
		entityDrawers.add(new EntityTransformDrawer(shapeRenderer, camera, PIXELS_PER_UNIT));
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
		Entity wall2 = entityFactory.createWall(new Vector2(3, 0.3f), new Vector3(0, -2, 0));
		entityManager.add(wall2);
		Entity wall3 = entityFactory.createWall(new Vector2(3, 0.3f), new Vector3(4, -2, 0));
		entityManager.add(wall3);
		List<Entity> targetmanEntities = entityFactory.createTargetman(new Vector3(1, 0, 0));
		targetman = targetmanEntities.get(targetmanEntities.size() - 1);
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
		final float jumpSpeed = 4;
		float moveVelocityX = 0;
		if (Gdx.input.isKeyPressed(Keys.A)) {
			moveVelocityX = -speed;
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			moveVelocityX = speed;
		}
		setMoveVelocityX(targetman, moveVelocityX);
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			if (groundedEntities.contains(targetman)) {
				targetman.get(TranslatePart.class).setVelocityY(jumpSpeed);
			}
		}
	}

	private void setMoveVelocityX(final Entity entity, final float moveVelocityX) {
		entity.get(TranslatePart.class).setVelocityX(moveVelocityX);
		if (moveVelocityX == 0) {
			entity.get(LimbAnimationsPart.class).get("walk").stop();
		} else {
			entity.get(LimbAnimationsPart.class).get("walk").play();
		}
		if (moveVelocityX > 0) {
			entity.get(LimbsPart.class).setFlipX(false);
		} else if (moveVelocityX < 0) {
			entity.get(LimbsPart.class).setFlipX(true);
		}
	}

}
