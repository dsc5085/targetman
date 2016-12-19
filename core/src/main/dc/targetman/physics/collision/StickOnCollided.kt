package dc.targetman.physics.collision

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.physics.DefaultTransform
import dclib.physics.collision.CollidedEvent
import dclib.util.FloatRange

class StickOnCollided(val entityManager: EntityManager) : (CollidedEvent) -> Unit {
	override fun invoke(event: CollidedEvent) {
		val deathTimeRange = FloatRange(10f, 120f)
		val sourceEntity = event.source.entity
		val targetBodyType = event.target.body.type
		if (sourceEntity.of(Material.STICKY) && targetBodyType === BodyType.StaticBody) {
			val spawn = Entity()
            val transform = sourceEntity[TransformPart::class].transform
			val spawnTransform = DefaultTransform(transform)
// TODO:
//			Vector2 size = transform.getSize();
//			if (Math.abs(offset.x) < size.x || Math.abs(offset.y) < size.y) {
//				Vector2 stickOffset = new Vector2(size.x * -Math.signum(offset.x), size.y * -Math.signum(offset.y));
//				transform.translate(stickOffset);
//			}
			val timedDeathPart = TimedDeathPart(deathTimeRange.random())
            spawn.attach(TransformPart(spawnTransform), sourceEntity[SpritePart::class], timedDeathPart)
			entityManager.add(spawn)
		}
	}
}