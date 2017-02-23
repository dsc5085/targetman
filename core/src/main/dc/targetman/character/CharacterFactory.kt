package dc.targetman.character

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.esotericsoftware.spine.Skeleton
import dc.targetman.ai.AiProfile
import dc.targetman.epf.parts.*
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityUtils
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.skeleton.*
import dc.targetman.util.Json
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.HealthPart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.size
import dclib.graphics.TextureCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.DefaultTransform
import dclib.util.inv

class CharacterFactory(
        private val entityManager: EntityManager,
        private val textureCache: TextureCache,
        private val world: World,
        private val limbFactory: LimbFactory
) {
    fun create(characterPath: String, height: Float, position: Vector3, alliance: Alliance): Entity {
        val character = Json.toObject<Character>(characterPath)
        val entity = Entity()
        entity.addAttributes(alliance)
        val atlas = textureCache.getAtlas(character.atlasName)
        val skeleton = SkeletonUtils.createSkeleton(character.skeletonPath, atlas)
        val skeletonScale = height / skeleton.bounds.size.y
        val size = skeleton.bounds.size.scl(skeletonScale)
        val body = createBody(size, position)
        val transform = Box2dTransform(body, position.z)
        entity.attach(TransformPart(transform))
        val skeletonPart = createSkeletonPart(skeleton, character, alliance, size)
        entity.attach(skeletonPart)
        entity.attach(FiringPart(character.rotatorName, character.muzzleName))
        val movementLimbNames = character.limbs.filter { it.isMovement }.map { it.name }
        entity.attach(MovementPart(8f, 9f, movementLimbNames))
        val vitalLimbNames = character.limbs.filter { it.isVital }.map { it.name }
        entity.attach(VitalLimbsPart(vitalLimbNames))
        entity.attach(HealthPart(character.health))
        val weaponAtlas = textureCache.getAtlas(character.weaponData.atlasName)
        val weapon = Weapon(character.weaponData, weaponAtlas)
        val inventoryPart = InventoryPart(1, "grip", character.gripperName, weapon)
        entity.attach(inventoryPart)
        if (alliance === Alliance.ENEMY) {
            val aiProfile = AiProfile(2f, 4.5f)
            entity.attach(AiPart(aiProfile))
        }
        entityManager.add(entity)
        entityManager.add(createWeaponEntity(skeletonPart["gripper"], weapon))
        return entity
    }

    private fun createSkeletonPart(
            skeleton: Skeleton,
            character: Character,
            alliance: Alliance,
            size: Vector2
    ): SkeletonPart {
        val root = limbFactory.create(skeleton, character.atlasName, size)
        val skeletonPart = SkeletonPart(root)
        for (limb in skeletonPart.getLimbs()) {
            val entity = limb.entity
            entity.addAttributes(DeathForm.CORPSE, alliance)
            val characterLimb = character.limbs.firstOrNull { it.name == limb.name }
            if (characterLimb != null) {
                setup(entity, characterLimb)
            }
        }
        return skeletonPart
    }

    private fun setup(entity: Entity, characterLimb: CharacterLimb) {
        entity.addAttributes(characterLimb.material)
        if (characterLimb.isPassive) {
            entity.removeAttributes(Alliance::class)
        }
        entity.attach(HealthPart(characterLimb.health))
        EntityUtils.filterSameAlliance(entity)
    }

    private fun createBody(size: Vector2, position: Vector3): Body {
        // Provide a small buffer to the base radius so that it doesn't collide with the wall and create friction
        val baseRadiusRatio = 0.99f
        val halfWidth = size.x / 2
        val boxHalfHeight = (size.y - halfWidth) / 2
        val def = BodyDef()
        def.type = BodyDef.BodyType.DynamicBody
        val body = world.createBody(def)
        body.isBullet = true
        body.isFixedRotation = true
        body.setTransform(position.x, position.y, 0f)
        val basePosition = Vector2(0f, -boxHalfHeight)
        createBaseFixtures(basePosition, body, halfWidth * baseRadiusRatio)
        val boxShape = PolygonShape()
        boxShape.setAsBox(halfWidth, boxHalfHeight)
        val bodyFixture = body.createFixture(boxShape, 1f)
        bodyFixture.friction = 0f
        boxShape.dispose()
        Box2dUtils.setFilter(body, CollisionCategory.BOUNDS, CollisionCategory.PROJECTILE.inv())
        return body
    }

    private fun createBaseFixtures(basePosition: Vector2, body: Body, radius: Float) {
        val numPerimeterPoints = 14
        val baseVertices = PolygonUtils.createCircleVertices(radius, basePosition, numPerimeterPoints)
        val baseVerticesPartitions = PolygonUtils.partition(baseVertices)
        for (baseVerticesPartition in baseVerticesPartitions) {
            val baseShape = PolygonShape()
            baseShape.set(baseVerticesPartition)
            val baseFixture = body.createFixture(baseShape, 0f)
            baseFixture.friction = 0.1f
            baseShape.dispose()
        }
    }

    private fun createWeaponEntity(gripper: Limb, weapon: Weapon): Entity {
        val root = limbFactory.create(weapon.skeleton, weapon.data.atlasName, weapon.size)
        val transform = DefaultTransform()
        val entity = Entity(SkeletonPart(root), TransformPart(transform))
        gripper.add(SkeletonLink(entity))
        return entity
    }
}