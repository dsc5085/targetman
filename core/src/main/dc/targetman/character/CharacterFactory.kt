package dc.targetman.character

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.esotericsoftware.spine.Skeleton
import dc.targetman.ai.AiProfile
import dc.targetman.epf.parts.*
import dc.targetman.level.FactoryTools
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityUtils
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.skeleton.BoundingSlotsPart
import dc.targetman.skeleton.LimbFactory
import dc.targetman.skeleton.SkeletonFactory
import dc.targetman.skeleton.getBounds
import dc.targetman.util.Json
import dclib.epf.Entity
import dclib.epf.parts.HealthPart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.size
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.util.FloatRange
import dclib.util.inv

class CharacterFactory(private val factoryTools: FactoryTools) {
    private val skeletonFactory = SkeletonFactory(factoryTools.textureCache)
    private val limbFactory = LimbFactory(factoryTools)

    fun create(characterPath: String, height: Float, position: Vector3, alliance: Alliance): Entity {
        val character = Json.toObject<Character>(characterPath)
        val entity = Entity()
        entity.addAttributes(alliance)
        val skeleton = skeletonFactory.create(character.skeletonPath, character.atlasName)
        val skeletonScale = height / skeleton.getBounds().size.y
        val size = skeleton.getBounds().size.scl(skeletonScale)
        val body = createBody(size, position)
        val transform = Box2dTransform(body, position.z)
        entity.attach(TransformPart(transform))
        val skeletonPart = createSkeletonPart(skeleton, character, alliance, size)
        entity.attach(skeletonPart)
        val weaponSkeleton = skeletonFactory.create(character.weaponData.skeletonPath, character.weaponData.atlasName)
        val weapon = Weapon(character.weaponData, weaponSkeleton)
        entity.attach(FiringPart(character.rotatorName, "muzzle"))
        val inventoryPart = InventoryPart(1, character.gripperName, weapon)
        entity.attach(inventoryPart)
        val movementLimbNames = character.limbs.filter { it.isMovement }.map { it.name }
        entity.attach(MovementPart(8f, 9f, movementLimbNames))
        val vitalLimbNames = character.limbs.filter { it.isVital }.map { it.name }
        entity.attach(VitalLimbsPart(vitalLimbNames))
        entity.attach(HealthPart(character.health))
        val boundingSlotNames = listOf("head", "left_foot", "right_foot", "torso")
        entity.attach(BoundingSlotsPart(boundingSlotNames))
        val shadowValueRange = FloatRange(0.9f, 1f)
        val keyLimbNames = listOf("left_hand", "left_foot", "right_thigh")
        entity.attach(LimbsShadowingPart(shadowValueRange, keyLimbNames))
        if (alliance == Alliance.ENEMY) {
            val aiProfile = AiProfile(2f, 4.5f)
            entity.attach(AiPart(aiProfile))
        }
        factoryTools.entityManager.add(entity)
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
        for (limb in skeletonPart.getLimbs(true)) {
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
        val body = factoryTools.world.createBody(def)
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
}