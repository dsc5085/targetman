package dc.targetman.mechanics

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

class CorpseOnLimbRemoved(val entityManager: EntityManager) : (LimbRemovedEvent) -> Unit {
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
            for (child in limb.getChildren()) {
                if (createCorpseChain(child) != null) {
                    createJoint(corpseTransform, child)
                }
            }
        }
        return corpseTransform
    }

    private fun createCorpseEntity(entity: Entity): Entity {
        val corpseLiveTime = 30f
        val transform = entity[TransformPart::class].transform as Box2dTransform
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

    private fun createJoint(corpseTransform: Box2dTransform, childLimb: Limb) {
        val jointDef = RevoluteJointDef()
        jointDef.collideConnected = false
        jointDef.bodyA = corpseTransform.body
        // TODO: THis is the child bone position relative to the parents axes
//		val localAnchorA = joint.parentLocal.scl(corpseTransform.scale)
//		jointDef.localAnchorA.set(localAnchorA)
//		jointDef.bodyB = childCorpseTransform.body
//		// TODO: THis is the bone position
//		val localAnchorB = joint.childLocal.scl(childCorpseTransform.scale)
//		jointDef.localAnchorB.set(localAnchorB)
//		corpseTransform.body.world.createJoint(jointDef)
    }

    private fun createCorpseTransform(transform: Box2dTransform): Box2dTransform {
        val corpseTransform = Box2dTransform(transform)
        val corpseBody = corpseTransform.body
        corpseBody.gravityScale = 1f
        Box2dUtils.setFilter(corpseBody, CollisionCategory.ALL, CollisionCategory.STATIC, 0)
        for (fixture in corpseBody.fixtureList) {
            fixture.isSensor = false
        }
        return corpseTransform
    }
}