package dc.targetman.character

import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.SkeletonBinary
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.ai.AiProfile
import dc.targetman.epf.parts.*
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.DeathForm
import dc.targetman.mechanics.EntityUtils
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.skeleton.Limb
import dc.targetman.skeleton.bounds
import dc.targetman.util.Json
import dclib.epf.Entity
import dclib.epf.parts.HealthPart
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.*
import dclib.graphics.TextureCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.DefaultTransform
import dclib.system.io.FileUtils

class CharacterFactory(private val textureCache: TextureCache, private val world: World) {
    fun create(characterPath: String, height: Float, position: Vector3, alliance: Alliance): Entity {
        val character = Json.toObject<Character>(FileUtils.toFileHandle(characterPath))
        val entity = Entity()
        entity.addAttributes(alliance)
        val skeleton = createSkeleton(character.skeletonPath, character.atlasName)
        val skeletonSize = skeleton.bounds.size
        val baseScaleValue = skeleton.rootBone.scaleY * height / skeletonSize.y
        val baseScale = Vector2(baseScaleValue, baseScaleValue)
        val body = createBody(skeletonSize.cpy().scl(baseScale), position)
        body.userData = entity
        val transform = Box2dTransform(position.z, body)
        entity.attach(TransformPart(transform))
        val limbEntities = createLimbs(character, skeleton, alliance, entity, baseScale)
        entity.attach(SkeletonPart(skeleton, limbEntities))
        entity.attach(WeaponPart(character.weapon, character.rotatorName, character.muzzleName))
        val movementLimbNames = character.limbs.filter { it.isMovement }.map { it.name }
        entity.attach(MovementPart(8f, 9f, movementLimbNames))
        val vitalLimbNames = character.limbs.filter { it.isVital }.map { it.name }
        entity.attach(VitalLimbsPart(vitalLimbNames))
        entity.attach(HealthPart(character.health))
        if (alliance === Alliance.ENEMY) {
            val aiProfile = AiProfile(2f, 4.5f)
            entity.attach(AiPart(aiProfile))
        }
        return entity
    }

    private fun createSkeleton(skeletonPath: String, atlasName: String): Skeleton {
        val atlas = textureCache.getAtlas(atlasName)
        val skeletonBinary = SkeletonBinary(atlas)
        val skeletonFile = FileUtils.toFileHandle(skeletonPath)
        val skeleton = Skeleton(skeletonBinary.readSkeletonData(skeletonFile))
        skeleton.updateWorldTransform()
        return skeleton
    }

    private fun createBody(size: Vector2, position: Vector3): Body {
        val halfWidth = size.x / 2
        val boxHalfHeight = (size.y - halfWidth) / 2
        val def = BodyDef()
        def.type = BodyDef.BodyType.DynamicBody
        val body = world.createBody(def)
        body.isBullet = true
        body.isFixedRotation = true
        body.setTransform(position.x, position.y, 0f)
        val basePosition = Vector2(0f, -boxHalfHeight)
        createBaseFixtures(basePosition, body, halfWidth)
        val boxShape = PolygonShape()
        boxShape.setAsBox(halfWidth, boxHalfHeight)
        val bodyFixture = body.createFixture(boxShape, 1f)
        bodyFixture.friction = 0f
        boxShape.dispose()
        Box2dUtils.setFilter(body, CollisionCategory.BOUNDS, CollisionCategory.PROJECTILE.inv())
        return body
    }

    private fun createBaseFixtures(basePosition: Vector2, body: Body, halfWidth: Float) {
        val numPerimeterPoints = 14
        val baseVertices = PolygonUtils.createCircleVertices(halfWidth, basePosition, numPerimeterPoints)
        val baseVerticesPartitions = PolygonUtils.partition(baseVertices)
        for (baseVerticesPartition in baseVerticesPartitions) {
            val baseShape = PolygonShape()
            baseShape.set(baseVerticesPartition)
            val baseFixture = body.createFixture(baseShape, 0f)
            baseFixture.friction = 0.1f
            baseShape.dispose()
        }
    }

    private fun createLimbs(
            character: Character,
            skeleton: Skeleton,
            alliance: Alliance,
            container: Entity,
            baseScale: Vector2
    ): List<Limb> {
        val limbs = mutableListOf<Limb>()
        for (bone in skeleton.bones) {
            val entity = createLimbEntity(alliance, bone, character, skeleton, baseScale)
            limbs.add(Limb(bone.data.name, entity, container))
        }
        return limbs
    }

    private fun createLimbEntity(
            alliance: Alliance,
            bone: Bone,
            character: Character,
            skeleton: Skeleton,
            baseScale: Vector2): Entity {
        val name = bone.data.name
        val entity: Entity
        val regionAttachment = getRegionAttachments(skeleton, bone).firstOrNull()
        if (regionAttachment != null) {
            val limb = character.limbs.single { it.name == name }
            val regionScale = Vector2(regionAttachment.scaleX, regionAttachment.scaleY)
            val size = Vector2(regionAttachment.width, regionAttachment.height).scl(baseScale).scl(regionScale.abs())
            val regionName = "${character.atlasName}/${regionAttachment.path}"
            val scale = VectorUtils.sign(regionScale)
            entity = createLimbEntity(limb, size, scale, regionName, alliance)
        } else {
            entity = createSimpleEntity(alliance, baseScale)
        }
        return entity
    }

    private fun getRegionAttachments(skeleton: Skeleton, bone: Bone): List<RegionAttachment> {
        val boneSlots = skeleton.slots.filter { it.bone === bone }
        return boneSlots.map { it.attachment }.filterIsInstance<RegionAttachment>()
    }

    private fun createLimbEntity(
            limb: CharacterLimb,
            size: Vector2,
            scale: Vector2,
            regionName: String,
            alliance: Alliance
    ): Entity {
        val entity = Entity()
        entity.addAttributes(limb.material, DeathForm.CORPSE)
        if (!limb.isPassive) {
            entity.addAttributes(alliance)
        }
        val region = textureCache.getPolygonRegion(regionName)
        val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
        val body = Box2dUtils.createDynamicBody(world, vertices, true)
        body.gravityScale = 0f
        body.userData = entity
        val transform = Box2dTransform(body)
        transform.scale = scale
        entity.attach(TransformPart(transform), SpritePart(region))
        entity.attach(HealthPart(limb.health))
        EntityUtils.filterSameAlliance(entity)
        return entity
    }

    private fun createSimpleEntity(alliance: Alliance, scale: Vector2): Entity {
        val entity = Entity()
        entity.addAttributes(alliance, DeathForm.CORPSE)
        // TODO: Is there a better solution for the comment below?
        // The width and height are fairly arbitrary, but the limb should be large enough such that its geometry
        // contains the bone positions of its children.  Meeting this constraint ensures things work correctly such as
        // Box2D joint connections.
        val polygon = Polygon(PolygonUtils.createRectangleVertices(0.1f, 0.1f))
        val transform = DefaultTransform(polygon, 0f)
        transform.scale = scale
        entity.attach(TransformPart(transform))
        return entity
    }
}