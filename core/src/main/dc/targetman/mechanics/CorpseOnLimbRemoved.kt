package dc.targetman.mechanics

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.skeleton.Limb
import dc.targetman.skeleton.LimbRemovedEvent
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.Transform
import dclib.util.FloatRange
import dclib.util.Maths

class CorpseOnLimbRemoved(val entityManager: EntityManager, val world: World) : (LimbRemovedEvent) -> Unit {
	override fun invoke(event: LimbRemovedEvent) {
        val limb = event.limb
        val corpseTransform = createCorpseChain(limb)
        if (corpseTransform != null) {
            corpseTransform.velocity = limb.container[TransformPart::class].transform.velocity
        }
	}

    private fun createCorpseChain(limb: Limb): Box2dTransform? {
        var corpseTransform: Box2dTransform? = null
        if (limb.entity.of(DeathForm.CORPSE)) {
            val corpseEntity = createCorpseEntity(limb.entity)
            entityManager.add(corpseEntity)
            corpseTransform = corpseEntity[TransformPart::class].transform as Box2dTransform
            for (childLimb in limb.getChildren()) {
                val childCorpseTransform = createCorpseChain(childLimb)
                if (childCorpseTransform != null) {
                    createJoint(corpseTransform, childLimb, childCorpseTransform)
                }
            }
        }
        return corpseTransform
    }

    private fun createCorpseEntity(entity: Entity): Entity {
        val corpseLiveTime = 30f
        val transform = entity[TransformPart::class].transform
        val corpseTransform = createCorpseTransform(transform)
        val transformPart = TransformPart(corpseTransform)
        val timedDeathPart = TimedDeathPart(corpseLiveTime)
        val corpseEntity = Entity(transformPart, timedDeathPart)
        val spritePart = entity.tryGet(SpritePart::class)
        if (spritePart != null) {
            corpseEntity.attach(spritePart)
        }
        return corpseEntity
    }

    private fun createJoint(parentTransform: Box2dTransform, childLimb: Limb, childTransform: Box2dTransform) {
        val jointDef = RevoluteJointDef()
        val boneWorld = Vector2(childLimb.bone.worldX, childLimb.bone.worldY)
        jointDef.initialize(parentTransform.body, childTransform.body, boneWorld)
        jointDef.collideConnected = true
        jointDef.enableLimit = true
        val angleRange = getAngleRange(childLimb.name)
        val rawLowerAngle = (angleRange.min() - childLimb.bone.rotation)
        val rawUpperAngle = (angleRange.max() - childLimb.bone.rotation)
        val degreesClampOffset = rawUpperAngle - rawUpperAngle % Maths.DEGREES_MAX
        jointDef.lowerAngle = (rawLowerAngle - degreesClampOffset) * MathUtils.degRad
        jointDef.upperAngle = (rawUpperAngle - degreesClampOffset) * MathUtils.degRad
        parentTransform.body.world.createJoint(jointDef)
    }

    // TODO: Put these values in a file
    private fun getAngleRange(limbName: String): FloatRange {
        val lowerAngle = when (limbName) {
            "head" -> -30f
            "neck" -> -60f
            "torso" -> 15f
            "right_hand" -> -85f
            "right_forearm" -> 0f
            "right_bicep" -> -180f
            "right_foot" -> 45f
            "right_shin" -> -135f
            "right_thigh" -> 200f
            "left_hand" -> -85f
            "left_forearm" -> 0f
            "left_bicep" -> -180f
            "left_foot" -> 45f
            "left_shin" -> -135f
            "left_thigh" -> 200f
            else -> 0f
        }
        val upperAngle = when (limbName) {
            "head" -> 30f
            "neck" -> 15f
            "torso" -> 100f
            "right_hand" -> 85f
            "right_forearm" -> 150f
            "right_bicep" -> 180f
            "right_foot" -> 100f
            "right_shin" -> 0f
            "right_thigh" -> 360f
            "left_hand" -> 85f
            "left_forearm" -> 150f
            "left_bicep" -> 180f
            "left_foot" -> 100f
            "left_shin" -> 0f
            "left_thigh" -> 360f
            else -> 0f
        }
        return FloatRange(lowerAngle, upperAngle)
    }

    private fun createCorpseTransform(transform: Transform): Transform {
        val corpseTransform = createBox2dTransform(transform)
        val corpseBody = corpseTransform.body
        corpseBody.gravityScale = 1f
        Box2dUtils.setFilter(corpseBody, CollisionCategory.ALL, CollisionCategory.STATIC)
        for (fixture in corpseBody.fixtureList) {
            fixture.isSensor = false
        }
        return corpseTransform
    }

    private fun createBox2dTransform(transform: Transform): Box2dTransform {
        val corpseTransform: Box2dTransform
        if (transform is Box2dTransform) {
            corpseTransform = Box2dTransform(transform)
        } else {
            // TODO: Move this code into a new constructor?
            val body = Box2dUtils.createDynamicBody(world, transform.getVertices())
            corpseTransform = Box2dTransform(body)
            corpseTransform.position = transform.position
            corpseTransform.rotation = transform.rotation
        }
        return corpseTransform
    }
}