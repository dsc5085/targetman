package dc.targetman.mechanics

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import dc.targetman.physics.collision.CollisionCategory
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils
import dclib.physics.limb.Joint
import dclib.physics.limb.Limb
import dclib.physics.limb.LimbRemovedListener
import net.dermetfan.gdx.physics.box2d.Box2DUtils

class CorpseOnLimbRemoved(entityManager: EntityManager): LimbRemovedListener {
	private val entityManager: EntityManager = entityManager
	
	override fun removed(limb: Limb) {
		createCorpse(limb)
	}
	
	private fun createCorpse(limb: Limb): Box2dTransform? {
		val corpseLiveTime = 30f
		var corpseTransform: Box2dTransform? = null
		val transform = limb.entity[TransformPart::class.java].transform
		if (transform is Box2dTransform && limb.entity.of(DeathForm.CORPSE)) {
			corpseTransform = createCorpseBody(transform)
			for (joint in limb.joints) {
				createChildCorpse(corpseTransform, joint)
			}
			val transformPart = TransformPart(corpseTransform)
			val timedDeathPart = TimedDeathPart(corpseLiveTime)
			val corpseEntity = Entity(transformPart, timedDeathPart)
			val spritePart = limb.entity.tryGet(SpritePart::class.java)
			if (spritePart != null) {
				corpseEntity.attach(spritePart)
			}
			entityManager.add(corpseEntity)
		}
		return corpseTransform
	}
	
	private fun createChildCorpse(corpseTransform: Box2dTransform, joint: Joint) {
		val childCorpseTransform = createCorpse(joint.limb)
		if (childCorpseTransform != null) {
			val jointDef = RevoluteJointDef()
			jointDef.collideConnected = true
			jointDef.bodyA = corpseTransform.body
			val localAnchorA = joint.parentLocal.scl(corpseTransform.scale)
			jointDef.localAnchorA.set(localAnchorA)
			jointDef.bodyB = childCorpseTransform.body
			val localAnchorB = joint.childLocal.scl(childCorpseTransform.scale)
			jointDef.localAnchorB.set(localAnchorB)
			corpseTransform.body.world.createJoint(jointDef)
		}
	}
	
	private fun createCorpseBody(transform: Box2dTransform): Box2dTransform {
		val corpseTransform = Box2dTransform(transform)
		val corpseBody = corpseTransform.body
		corpseBody.gravityScale = 1f
		Box2dUtils.setFilter(corpseBody, CollisionCategory.ALL, CollisionCategory.STATIC)
		for (fixture in corpseBody.fixtureList) {
			fixture.isSensor = false
		}
		return corpseTransform
	}
	
}