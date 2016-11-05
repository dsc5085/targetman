package dc.targetman.mechanics

import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntityRemovedListener
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.system.Updater
import net.dermetfan.gdx.physics.box2d.Box2DUtils

class CorpseSystem(entityManager: EntityManager) : Updater {
	private val entityManager: EntityManager = entityManager
	private var corpses: MutableSet<Entity> = mutableSetOf()
	
	init {
		entityManager.listen(entityRemoved())
	}
	
	override fun update(delta: Float) {
		// TODO: add joints between related corpses
		for (corpse in corpses) {
			entityManager.add(corpse)
		}
		corpses.clear()
	}
	
	private fun entityRemoved(): EntityRemovedListener {
		return object : EntityRemovedListener {
			override fun removed(entity: Entity) {
				val transform = entity.tryGet(TransformPart::class.java)?.transform
				val spritePart = entity.tryGet(SpritePart::class.java)
				if (entity.of(DeathForm.CORPSE) && transform is Box2dTransform && spritePart != null) {
					corpses.add(createCorpse(transform, spritePart))
				}
			}
		}
	}
	
	private fun createCorpse(transform: Box2dTransform, spritePart: SpritePart): Entity {
		val body = transform.body
		val clonedBody = body.world.createBody(Box2DUtils.createDef(body));
		clonedBody.gravityScale = 1f
		for(fixture in body.fixtureList) {
			Box2DUtils.clone(fixture, clonedBody, true).isSensor = false;
		}
		val transformPart = TransformPart(Box2dTransform(transform.z, clonedBody))
		// TODO: Use variable instead of literal
		val timedDeathPart = TimedDeathPart(30f)
		return Entity(transformPart, spritePart, timedDeathPart)
	}
	
}