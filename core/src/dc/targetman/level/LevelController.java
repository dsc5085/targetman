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

import dc.targetman.epf.parts.WeaponPart;
import dc.targetman.epf.systems.ScaleSystem;
import dc.targetman.epf.systems.WeaponSystem;
import dc.targetman.level.models.CollisionGroup;
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
import dclib.epf.parts.PhysicsPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
import dclib.epf.systems.AutoRotateSystem;
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
import dclib.physics.BodyType;
import dclib.physics.CollidedListener;
import dclib.system.Advancer;

public final class LevelController {

	private static final float PIXELS_PER_UNIT = 32f;

	private final EntityFactory entityFactory;
	private final EntityManager entityManager = new DefaultEntityManager();
	// TODO: Get rid of entitySystemManager and just make a single generic update interface
	private final EntitySystemManager entitySystemManager;
	private final PhysicsSystem physicsSystem;
	private final Advancer advancer;
	private final Camera camera;
	private final UnitConverter unitConverter;
	private final ParticlesManager particlesManager;
	private final List<EntityDrawer> entityDrawers = new ArrayList<EntityDrawer>();
	private final Set<Entity> groundedEntities = new HashSet<Entity>();
	private Entity targetman;

	public LevelController(final TextureCache textureCache, final PolygonSpriteBatch spriteBatch,
			final ShapeRenderer shapeRenderer) {
		camera = new OrthographicCamera(320, 240);
		unitConverter = new UnitConverter(PIXELS_PER_UNIT, camera);
		particlesManager = new ParticlesManager(textureCache, camera, spriteBatch, unitConverter);
		entityFactory = new EntityFactory(entityManager, textureCache);
		entitySystemManager = createEntitySystemManager();
		physicsSystem = new PhysicsSystem(entityManager, -8);
		physicsSystem.addCollidedListener(collided());
		physicsSystem.addCollidedListener(new DamageCollidedListener());
		// TODO: Remove entity drawer.  Create generic drawer where i can add particles drawing
		entityDrawers.add(new EntitySpriteDrawer(spriteBatch, camera, entityManager));
		entityDrawers.add(new EntityTransformDrawer(shapeRenderer, camera, PIXELS_PER_UNIT));
		entityManager.addEntityAddedListener(new RemoveOnNoHealthEntityAddedListener(entityManager));
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
		particlesManager.draw();
		List<Entity> entities = entityManager.getAll();
		for (EntityDrawer entityDrawer : entityDrawers) {
			entityDrawer.draw(entities);
		}
	}

	private EntitySystemManager createEntitySystemManager() {
		EntitySystemManager entitySystemManager = new DefaultEntitySystemManager(entityManager);
		entitySystemManager.add(new TranslateSystem());
		entitySystemManager.add(new AutoRotateSystem());
		entitySystemManager.add(new ScaleSystem());
		entitySystemManager.add(new LimbsSystem(entityManager));
		entitySystemManager.add(new TimedDeathSystem(entityManager));
		entitySystemManager.add(new WeaponSystem(entityFactory));
		entitySystemManager.add(new DrawableSystem(unitConverter));
		return entitySystemManager;
	}

	private CollidedListener collided() {
		return new CollidedListener() {
			@Override
			public void collided(final Entity collider, final Entity collidee, final Vector2 offset) {
				if (offset.y > 0) {
					groundedEntities.add(collider);
				}
				PhysicsPart physicsPart1 = collider.get(PhysicsPart.class);
				PhysicsPart physicsPart2 = collidee.get(PhysicsPart.class);
				if (physicsPart1.containsAny(CollisionGroup.BULLET.ordinal())
						&& physicsPart2.getBodyType() == BodyType.STATIC) {
					entityManager.remove(collider);
					Vector2 position = collider.get(TransformPart.class).getCenter();
					particlesManager.createEffect("spark", position);
				}
			}
		};
	}

	private void spawnInitialEntities() {
		entityFactory.createWall(new Vector2(2f, 3), new Vector3(-2, -2, 0));
		entityFactory.createWall(new Vector2(3, 0.3f), new Vector3(0, -2, 0));
		entityFactory.createWall(new Vector2(3, 0.3f), new Vector3(4, -2, 0));
		targetman = entityFactory.createStickman(new Vector3(4, 0, 0));
		entityFactory.createStickman(new Vector3(6, 0, 0));
	}

	private Advancer createAdvancer() {
		return new Advancer() {
			@Override
			protected void update(final float delta) {
				entitySystemManager.update(delta);
				physicsSystem.update(delta);
				particlesManager.update(delta);
			}
		};
	}

	private void processInput() {
		final float speed = 5;
		final float jumpSpeed = 5;
		float moveVelocityX = 0;
		if (Gdx.input.isKeyPressed(Keys.A)) {
			moveVelocityX = -speed;
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			moveVelocityX = speed;
		}
		setMoveVelocityX(targetman, moveVelocityX);
		float aimRotateMultiplier = 0;
		if (Gdx.input.isKeyPressed(Keys.W)){
			aimRotateMultiplier = 1;
		} else if (Gdx.input.isKeyPressed(Keys.S)) {
			aimRotateMultiplier = -1;
		}
		targetman.get(WeaponPart.class).setRotateMultiplier(aimRotateMultiplier);
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			if (groundedEntities.contains(targetman)) {
				targetman.get(TranslatePart.class).setVelocityY(jumpSpeed);
			}
		}
		if (Gdx.input.isKeyPressed(Keys.J)){
			targetman.get(WeaponPart.class).setTriggered(true);
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
