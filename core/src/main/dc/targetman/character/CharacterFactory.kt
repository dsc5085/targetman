package dc.targetman.character

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.*
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.ai.AiProfile
import dc.targetman.epf.parts.*
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.DeathForm
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.physics.PhysicsUtils
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.skeleton.bounds
import dclib.epf.Entity
import dclib.epf.parts.HealthPart
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.PolygonUtils
import dclib.geometry.VectorUtils
import dclib.geometry.inv
import dclib.geometry.size
import dclib.graphics.TextureCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.DefaultTransform

class CharacterFactory(
        private val characterLoader: CharacterLoader,
        private val textureCache: TextureCache,
        private val world: World) {
    fun create(skeletonPath: String, height: Float, position: Vector3, alliance: Alliance): Entity {
        val character = characterLoader.create(skeletonPath)
        val entity = Entity()
        entity.attribute(alliance)
        val skeleton = character.skeleton
        val baseScaleValue = skeleton.rootBone.scaleY * height / skeleton.bounds.height
        val baseScale = Vector2(baseScaleValue, baseScaleValue)
        val body = createBody(skeleton.bounds.size.cpy().scl(baseScale), position)
        body.userData = entity
        val transform = Box2dTransform(position.z, body)
        entity.attach(TransformPart(transform))
        val limbEntities = createLimbEntities(character, alliance)
        entity.attach(SkeletonPart(skeleton, baseScale, limbEntities))
        val target = alliance.target.name
        val weapon = Weapon(0.1f, 1, 35f, 28f, 32f, 0f, target)
        entity.attach(WeaponPart(weapon, character.rotatorName, character.muzzleName))
        val movementLimbNames = character.limbs.filter { it.isMovement }.map { it.name }
        entity.attach(MovementPart(8f, 9f, movementLimbNames))
        val vitalLimbNames = character.limbs.filter { it.isVital }.map { it.name }
        entity.attach(VitalLimbsPart(vitalLimbNames))
        if (alliance === Alliance.ENEMY) {
            val aiProfile = AiProfile(2f, 4.5f)
            entity.attach(AiPart(aiProfile))
        }
        return entity
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
        val baseShape = CircleShape()
        baseShape.radius = halfWidth
        baseShape.position = Vector2(0f, -boxHalfHeight)
        val baseFixture = body.createFixture(baseShape, 0f)
        baseFixture.friction = 0.1f
        baseShape.dispose()
        val boxShape = PolygonShape()
        boxShape.setAsBox(halfWidth, boxHalfHeight)
        val bodyFixture = body.createFixture(boxShape, 1f)
        bodyFixture.friction = 0f
        boxShape.dispose()
        Box2dUtils.setFilter(body, CollisionCategory.BOUNDS, CollisionCategory.PROJECTILE.inv())
        return body
    }

    private fun createLimbEntities(character: Character, alliance: Alliance): Map<String, Entity> {
        val limbEntities = mutableMapOf<String, Entity>()
        val skeleton = character.skeleton
        for (bone in skeleton.bones) {
            val regionAttachment = getRegionAttachments(skeleton, bone).firstOrNull()
            val name = bone.data.name
            val entity: Entity
            if (regionAttachment != null) {
                val limb = character.limbs.single { it.name == name }
                val regionScale = Vector2(regionAttachment.scaleX, regionAttachment.scaleY)
                val size = Vector2(regionAttachment.width, regionAttachment.height)
                        .scl(VectorUtils.abs(regionScale))
                val regionName = "${character.atlasName}/${regionAttachment.path}"
                val scale = VectorUtils.sign(regionScale)
                entity = createLimbEntity(limb, size, scale, regionName, alliance)
            } else {
                entity = createSimpleEntity(alliance)
            }
            limbEntities.put(name, entity)
        }
        return limbEntities
    }

    private fun getRegionAttachments(skeleton: Skeleton, bone: Bone): List<RegionAttachment> {
        val boneSlots = skeleton.slots.filter { it.bone === bone }
        return boneSlots.map { it.attachment }.filterIsInstance<RegionAttachment>()
    }

    private fun createSimpleEntity(alliance: Alliance): Entity {
        val entity = Entity()
        entity.attribute(alliance)
        val transform = DefaultTransform()
        entity.attach(TransformPart(transform))
        return entity
    }

    private fun createLimbEntity(limb: CharacterLimb, size: Vector2, scale: Vector2, regionName: String, alliance: Alliance): Entity {
        val entity = Entity()
        entity.attribute(alliance, limb.material, DeathForm.CORPSE)
        val region = textureCache.getPolygonRegion(regionName)
        val vertices = PolygonUtils.createRectangleVertices(size.x, size.y)
        val body = PhysicsUtils.createDynamicBody(world, vertices, true)
        body.gravityScale = 0f
        body.userData = entity
        val transform = Box2dTransform(body)
        transform.scale = scale
        entity.attach(TransformPart(transform), SpritePart(region))
        val group = (-Box2dUtils.toGroup(alliance)).toShort()
        Box2dUtils.setFilter(body, group = group)
        entity.attach(HealthPart(limb.health))
        return entity
    }
}