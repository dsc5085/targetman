package dc.targetman.character

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.*
import com.esotericsoftware.spine.attachments.RegionAttachment
import dc.targetman.ai.AiProfile
import dc.targetman.epf.parts.*
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.DeathForm
import dc.targetman.mechanics.weapon.Weapon
import dc.targetman.physics.PhysicsUtils
import dc.targetman.physics.collision.CollisionCategory
import dclib.epf.Entity
import dclib.epf.parts.HealthPart
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TransformPart
import dclib.geometry.inv
import dclib.graphics.ConvexHullCache
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.DefaultTransform

class CharacterFactory(
        private val characterLoader: CharacterLoader,
        private val convexHullCache: ConvexHullCache,
        private val world: World) {
    fun create(skeletonPath: String, position: Vector3, alliance: Alliance): Entity {
        val character = characterLoader.create(skeletonPath)
        val entity = Entity()
        entity.attribute(alliance)
        val size = Vector2(Box2dUtils.ROUNDING_ERROR, Box2dUtils.ROUNDING_ERROR)
        val body = createBody(size, position)
        body.userData = entity
        val transform = Box2dTransform(position.z, body)
        entity.attach(TransformPart(transform))
        val limbEntities = createLimbEntities(character, alliance)
        entity.attach(SkeletonPart(character.skeleton, limbEntities))
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
        val boxHalfHeight = size.y - halfWidth
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
        for (bone in character.skeleton.bones) {
            val boneSlots = character.skeleton.slots.filter { it.bone === bone }
            val regionAttachment = boneSlots.map { it.attachment }.filterIsInstance<RegionAttachment>().firstOrNull()
            val name = bone.data.name
            val entity: Entity
            if (regionAttachment != null) {
                val limb = character.limbs.single { it.name == name }
                val size = Vector2(regionAttachment.width, regionAttachment.height)
                        .scl(bone.worldScaleX, bone.worldScaleY)
                val regionName = "${character.atlasName}/${regionAttachment.path}"
                entity = createLimbEntity(limb, size, regionName, alliance)
            } else {
                entity = createSimpleEntity(alliance)
            }
            limbEntities.put(name, entity)
        }
        return limbEntities
    }

    private fun createSimpleEntity(alliance: Alliance): Entity {
        val entity = Entity()
        entity.attribute(alliance)
        val transform = DefaultTransform()
        entity.attach(TransformPart(transform))
        return entity
    }

    private fun createLimbEntity(limb: Limb, size: Vector2, regionName: String, alliance: Alliance): Entity {
        val entity = Entity()
        entity.attribute(alliance, limb.material, DeathForm.CORPSE)
        val hullData = convexHullCache.create(regionName, size)
        val body = PhysicsUtils.createDynamicBody(world, hullData.hull, true)
        body.gravityScale = 0f
        body.userData = entity
        val transform = Box2dTransform(body)
        entity.attach(TransformPart(transform), SpritePart(hullData.region))
        Box2dUtils.setFilter(body, group = (-alliance.ordinal).toShort())
        entity.attach(HealthPart(limb.health))
        return entity
    }
}