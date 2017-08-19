package dc.targetman.character

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.esotericsoftware.spine.Skeleton
import dc.targetman.ai.AiProfile
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.InventoryPart
import dc.targetman.epf.parts.LimbsShadowingPart
import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.StaggerPart
import dc.targetman.epf.parts.VitalLimbsPart
import dc.targetman.level.FactoryTools
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityUtils
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.skeleton.BoundingSlotsPart
import dc.targetman.skeleton.Limb
import dc.targetman.skeleton.LimbFactory
import dc.targetman.skeleton.LinkType
import dc.targetman.skeleton.SkeletonFactory
import dc.targetman.skeleton.SkeletonUtils
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
import kotlin.experimental.inv

class CharacterFactory(private val factoryTools: FactoryTools) {
    private val DENSITY = 1f

    private val skeletonFactory = SkeletonFactory(factoryTools.textureCache)
    private val limbFactory = LimbFactory(factoryTools.world, factoryTools.textureCache)

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
        val inventoryPart = InventoryPart(2, character.gripperName, "frame", weapon)
        entity.attach(inventoryPart)
        val movementLimbNames = character.limbDatas.filter { it.isMovement }.map { it.name }
        val moveSpeed = Vector2(10f, 10f)
        entity.attach(MovementPart(moveSpeed, 0.1f, movementLimbNames))
        val vitalLimbNames = character.limbDatas.filter { it.isVital }.map { it.name }
        entity.attach(VitalLimbsPart(vitalLimbNames))
        entity.attach(HealthPart(character.health))
        entity.attach(StaggerPart(10f, character.stunResist, character.stunResist * 2))
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
        val rootScale = SkeletonUtils.calculateRootScale(skeleton, size)
        val root = limbFactory.create(skeleton, rootScale)
        characterizeDescendants(root.limb, character.limbDatas, alliance)
        return SkeletonPart(root)
    }

    private fun characterizeDescendants(rootLimb: Limb, limbDatas: List<CharacterLimbData>, alliance: Alliance) {
        for (limb in rootLimb.getDescendants(LinkType.STRONG)) {
            val limbData = limbDatas.firstOrNull { it.name == limb.name }
            if (limbData != null) {
                characterize(limb.entity, limbData, alliance)
            }
        }
    }

    private fun characterize(limbEntity: Entity, limbData: CharacterLimbData, alliance: Alliance) {
        limbEntity.addAttributes(DeathForm.CORPSE, limbData.material)
        if (!limbData.isPassive) {
            limbEntity.addAttributes(alliance)
        }
        val transform = limbEntity[TransformPart::class].transform
        if (transform is Box2dTransform) {
            Box2dUtils.setDensity(transform.body, DENSITY)
            Box2dUtils.setFriction(transform.body, 0.9f)
        }
        limbEntity.attach(HealthPart(limbData.health))
        EntityUtils.filterSameAlliance(limbEntity)
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
        val bodyFixture = body.createFixture(boxShape, DENSITY)
        // TODO: Make sure the mass of this body is equal to the total mass of the limbs
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
            val baseFixture = body.createFixture(baseShape, DENSITY)
            baseFixture.friction = 0.1f
            baseShape.dispose()
        }
    }
}