package dc.targetman.mechanics

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.limb.Joint
import dclib.physics.limb.Limb
import dclib.physics.limb.LimbRemovedListener
import dclib.physics.limb.LimbsSystem
import net.dermetfan.gdx.physics.box2d.Box2DUtils

class CorpseOnLimbRemoved(entityManager: EntityManager): LimbRemovedListener {
	private val entityManager: EntityManager = entityManager
	
	override fun removed(limb: Limb) {
		createCorpse(limb)
	}
	
	private fun createCorpse(limb: Limb): Body? {
		val corpseLiveTime = 30f
		var corpseBody: Body? = null
		val transform = limb.entity[TransformPart::class.java].transform
		if (transform is Box2dTransform && limb.entity.of(DeathForm.CORPSE)) {
			corpseBody = createCorpseBody(transform)
			for (joint in limb.joints) {
				createChildCorpse(corpseBody, joint)
			}
			val transformPart = TransformPart(Box2dTransform(transform.z, corpseBody))
			val timedDeathPart = TimedDeathPart(corpseLiveTime)
			val corpseEntity = Entity(transformPart, timedDeathPart)
			val spritePart = limb.entity.tryGet(SpritePart::class.java)
			if (spritePart != null) {
				corpseEntity.attach(spritePart)
			}
			entityManager.add(corpseEntity)
		}
		return corpseBody
	}
	
	private fun createChildCorpse(corpseBody: Body, joint: Joint) {
		val childCorpseBody = createCorpse(joint.limb)
		if (childCorpseBody != null) {
			val jointDef = RevoluteJointDef()
			jointDef.collideConnected = true
			jointDef.bodyA = corpseBody
			jointDef.localAnchorA.set(joint.parentLocal)
			jointDef.bodyB = childCorpseBody
			jointDef.localAnchorB.set(joint.childLocal)
			corpseBody.world.createJoint(jointDef)
		}
	}
	
	private fun createCorpseBody(transform: Box2dTransform): Body {
		val body = transform.body
		val corpseBody = body.world.createBody(Box2DUtils.createDef(body));
		corpseBody.gravityScale = 1f
		for (fixture in body.fixtureList) {
			Box2DUtils.clone(fixture, corpseBody, true).isSensor = false;
		}
		return corpseBody
	}
	
}