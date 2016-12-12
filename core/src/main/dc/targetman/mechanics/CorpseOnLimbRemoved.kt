package dc.targetman.mechanics

import dc.targetman.character.LimbRemovedEvent
import dclib.epf.EntityManager

class CorpseOnLimbRemoved(val entityManager: EntityManager) : (LimbRemovedEvent) -> Unit {
	override fun invoke(event: LimbRemovedEvent) {
        // TODO:
//        val corpseTransform = createCorpse(event)
//        if (corpseTransform != null) {
//            corpseTransform.velocity = event.container[TransformPart::class.java].transform.velocity
//        }
	}

//	private fun createCorpse(event: LimbRemovedEvent): Box2dTransform? {
//		val corpseLiveTime = 30f
//		var corpseTransform: Box2dTransform? = null
//		val entity = event.entity
//		val transform = entity[TransformPart::class.java].transform
//		if (transform is Box2dTransform && entity.of(DeathForm.CORPSE)) {
//			corpseTransform = createCorpseBody(transform)
//			for (joint in limb.joints) {
//				createChildCorpse(corpseTransform, joint)
//			}
//			val transformPart = TransformPart(corpseTransform)
//			val timedDeathPart = TimedDeathPart(corpseLiveTime)
//			val corpseEntity = Entity(transformPart, timedDeathPart)
//			val spritePart = entity.tryGet(SpritePart::class.java)
//			if (spritePart != null) {
//				corpseEntity.attach(spritePart)
//			}
//			entityManager.add(corpseEntity)
//		}
//		return corpseTransform
//	}
//
//	private fun createChildCorpse(corpseTransform: Box2dTransform, joint: Joint) {
//		val childCorpseTransform = createCorpse(joint.limb)
//		if (childCorpseTransform != null) {
//			val jointDef = RevoluteJointDef()
//            jointDef.collideConnected = false
//			jointDef.bodyA = corpseTransform.body
//			val localAnchorA = joint.parentLocal.scl(corpseTransform.scale)
//			jointDef.localAnchorA.set(localAnchorA)
//			jointDef.bodyB = childCorpseTransform.body
//			val localAnchorB = joint.childLocal.scl(childCorpseTransform.scale)
//			jointDef.localAnchorB.set(localAnchorB)
//			corpseTransform.body.world.createJoint(jointDef)
//		}
//	}
//
//	private fun createCorpseBody(transform: Box2dTransform): Box2dTransform {
//		val corpseTransform = Box2dTransform(transform)
//		val corpseBody = corpseTransform.body
//		corpseBody.gravityScale = 1f
//        Box2dUtils.setFilter(corpseBody, CollisionCategory.ALL, CollisionCategory.STATIC, 0)
//		for (fixture in corpseBody.fixtureList) {
//			fixture.isSensor = false
//		}
//		return corpseTransform
//	}
}