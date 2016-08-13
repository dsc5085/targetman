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
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Intersector.MinimumTranslationVector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import dclib.epf.DefaultEntityManager;
import dclib.epf.DefaultEntitySystemManager;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntitySystemManager;
import dclib.epf.graphics.EntityDrawer;
import dclib.epf.graphics.EntitySpriteDrawer;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.epf.systems.DrawableSystem;
import dclib.epf.systems.TranslateSystem;
import dclib.geometry.UnitConverter;
import dclib.graphics.CameraUtils;
import dclib.graphics.TextureCache;
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
		CameraUtils.centerOn(targetman, unitConverter, camera);
	}

	public final void draw() {
		List<Entity> entities = entityManager.getAll();
		for (EntityDrawer entityDrawer : entityDrawers) {
			entityDrawer.draw(entities);
		}
	}

	private EntitySystemManager createEntitySystemManager() {
		EntitySystemManager entitySystemManager = new DefaultEntitySystemManager(entityManager);
		entitySystemManager.add(new DrawableSystem(unitConverter));
		entitySystemManager.add(new TranslateSystem());
		return entitySystemManager;
	}

	private void spawnInitialEntities() {
		Entity wall = entityFactory.createTargetman(new Vector2(2f, 3), new Vector3(-2, -2, 0));
		entityManager.add(wall);
		Entity wall2 = entityFactory.createTargetman(new Vector2(3, 0.05f), new Vector3(0, -2, 0));
		entityManager.add(wall2);
		Entity wall3 = entityFactory.createTargetman(new Vector2(3, 0.05f), new Vector3(4, -2, 0));
		entityManager.add(wall3);
		targetman = entityFactory.createTargetman(new Vector2(1, 1), new Vector3());
		entityManager.add(targetman);
	}

	private Advancer createAdvancer() {
		return new Advancer() {
			@Override
			protected void update(final float delta) {
				entitySystemManager.update(delta);
				applyGravity(delta);
				processObstacleCollisions();
			}
		};
	}

	private void applyGravity(final float delta) {
		final float gravity = -8;
		targetman.get(TranslatePart.class).addVelocity(0, gravity * delta);
	}

	private void processObstacleCollisions() {
		for (Entity obstacle : entityManager.getAll()) {
			if (obstacle != targetman) {
				Polygon obstaclePolygon = obstacle.get(TransformPart.class).getPolygon();
				processObstacleCollision(targetman, obstaclePolygon);
			}
		}
	}

	private void processObstacleCollision(final Entity movingEntity, final Polygon obstaclePolygon) {
		final float bounceDampening = 0.0001f;
		TransformPart transformPart = movingEntity.get(TransformPart.class);
		MinimumTranslationVector translation = new MinimumTranslationVector();
		if (Intersector.overlapConvexPolygons(transformPart.getPolygon(), obstaclePolygon, translation)) {
			TranslatePart translatePart = movingEntity.get(TranslatePart.class);
			Vector2 velocity = translatePart.getVelocity();
			if (translation.normal.x != 0) {
				translatePart.setVelocityX(-velocity.x * bounceDampening);
			}
			float normalXSign = -Math.signum(velocity.x);
			float normalYSign = -Math.signum(velocity.y);
			if (translation.normal.y != 0) {
				translatePart.setVelocityY(-velocity.y * bounceDampening);
				if (normalYSign > 0) {
					groundedEntities.add(movingEntity);
				}
			}
			Vector2 offset = translation.normal.cpy().scl(normalXSign, normalYSign);
			offset.setLength(translation.depth);
			transformPart.translate(offset);
		}
	}

	private void processInput() {
		final float speed = 3;
		final float jumpSpeed = 4;
		TranslatePart translatePart = targetman.get(TranslatePart.class);
		float velocityX = 0;
		if (Gdx.input.isKeyPressed(Keys.A)) {
			velocityX = -speed;
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			velocityX = speed;
		}
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			if (groundedEntities.contains(targetman)) {
				translatePart.setVelocityY(jumpSpeed);
			}
		}
		translatePart.setVelocityX(velocityX);
	}

}
