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
import dc.targetman.epf.systems.RemoveCollidedListener;
import dc.targetman.epf.systems.ScaleSystem;
import dc.targetman.epf.systems.VitalLimbsSystem;
import dc.targetman.epf.systems.WeaponSystem;
import dc.targetman.level.models.Alliance;
import dc.targetman.level.models.CollisionType;
import dclib.epf.DefaultEntityManager;
import dclib.epf.Entity;
import dclib.epf.EntityManager;
import dclib.epf.EntityRemovedListener;
import dclib.epf.graphics.EntityDrawer;
import dclib.epf.graphics.EntitySpriteDrawer;
import dclib.epf.parts.LimbAnimationsPart;
import dclib.epf.parts.LimbsPart;
import dclib.epf.parts.PhysicsPart;
import dclib.epf.parts.TransformPart;
import dclib.epf.parts.TranslatePart;
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
import dclib.physics.BodyType;
import dclib.physics.CollidedListener;
import dclib.system.Advancer;

public final class LevelController {

	private static final float PIXELS_PER_UNIT = 32f;

	private final EntityFactory entityFactory;
	private final EntityManager entityManager = new DefaultEntityManager();
	private final CollisionSystem collisionSystem;
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
		collisionSystem = createCollisionSystem();
		// TODO: Remove entity drawer.  Create generic drawer where i can add particles drawing
		entityDrawers.add(new EntitySpriteDrawer(spriteBatch, camera, entityManager));
//		entityDrawers.add(new EntityTransformDrawer(shapeRenderer, camera, PIXELS_PER_UNIT));
		entityManager.addEntityAddedListener(new RemoveOnNoHealthEntityAddedListener(entityManager));
		entityManager.addEntityRemovedListener(entityRemoved());
		advancer = createAdvancer();
		spawnInitialEntities();
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

	private EntityRemovedListener entityRemoved() {
		return new EntityRemovedListener() {
			@Override
			public void removed(final Entity entity) {
				PhysicsPart physicsPart = entity.tryGet(PhysicsPart.class);
				if (physicsPart != null) {
					if (physicsPart.containsAny(CollisionType.BULLET)) {
						Vector2 position = entity.get(TransformPart.class).getCenter();
						particlesManager.createEffect("spark", position);
					}
				}
			}
		};
	}

	private CollidedListener collided() {
		return new CollidedListener() {
			@Override
			public void collided(final Entity collider, final Entity collidee, final Vector2 offset) {
				PhysicsPart collideePhysicsPart = collidee.get(PhysicsPart.class);
				if (collideePhysicsPart.getBodyType() == BodyType.STATIC && offset.y > 0) {
					groundedEntities.add(collider);
				}
			}
		};
	}

	private CollisionSystem createCollisionSystem() {
		CollisionSystem collisionSystem = new CollisionSystem(entityManager);
		collisionSystem.addCollidedListener(collided());
		collisionSystem.addCollidedListener(new DamageCollidedListener());
		collisionSystem.addCollidedListener(new RemoveCollidedListener(entityManager));
		return collisionSystem;
	}

	private Advancer createAdvancer() {
		return new Advancer()
		.add(new ScaleSystem(entityManager))
		.add(new AutoRotateSystem(entityManager))
		.add(new TranslateSystem(entityManager))
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
		entityFactory.createWall(new Vector2(2f, 3), new Vector3(-2, -2, 0));
		entityFactory.createWall(new Vector2(3, 0.3f), new Vector3(0, -2, 0));
		entityFactory.createWall(new Vector2(3, 0.3f), new Vector3(4, -2, 0));
		entityFactory.createWall(new Vector2(0.3f, 3), new Vector3(7, -2, 0));
		targetman = entityFactory.createStickman(new Vector3(4, 0, 0), Alliance.PLAYER);
		entityFactory.createStickman(new Vector3(6, 0, 0), Alliance.ENEMY);
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
