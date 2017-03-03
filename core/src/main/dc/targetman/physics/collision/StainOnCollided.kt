package dc.targetman.physics.collision

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.physics.DefaultTransform
import dclib.physics.collision.CollidedEvent
import dclib.util.FloatRange

class StainOnCollided(val entityManager: EntityManager) : (CollidedEvent) -> Unit {
	override fun invoke(event: CollidedEvent) {
		val sourceEntity = event.source.entity
		val targetBodyType = event.target.body.type
		if (sourceEntity.of(Material.STICKY) && targetBodyType === BodyType.StaticBody) {
            val dynamicTransform = sourceEntity[TransformPart::class].transform
            val staticTransform = event.target.entity[TransformPart::class].transform
            val edgeContact = CollisionHelper.getEdgeContact(dynamicTransform, staticTransform)
            if (edgeContact != null) {
                createStain(sourceEntity, edgeContact)
            }
        }
    }

    private fun createStain(particle: Entity, edgeContact: EdgeContact) {
        val stainScale = Vector2(2f, 0.5f)
        val deathTimeRange = FloatRange(10f, 120f)
        val stain = Entity()
        val transform = particle[TransformPart::class].transform
        val stainTransform = DefaultTransform(transform)
        stainTransform.setScale(stainTransform.scale.scl(stainScale))
        stainTransform.rotation = edgeContact.edge.angle
        stainTransform.setWorld(stainTransform.center, edgeContact.intersection)
        val timedDeathPart = TimedDeathPart(deathTimeRange.random())
        stain.attach(TransformPart(stainTransform), particle[SpritePart::class], timedDeathPart)
        entityManager.add(stain)
	}
}