package dc.targetman.character

import com.badlogic.gdx.physics.box2d.World
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.skeleton.LimbRemovedEvent
import dc.targetman.skeleton.Ragdoller
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class CorpseOnLimbRemoved(val entityManager: EntityManager, val world: World) : (LimbRemovedEvent) -> Unit {
    private val ragdoller = Ragdoller()

    override fun invoke(event: LimbRemovedEvent) {
        val corpseLiveTime = 30f
        if (event.limb.entity.of(DeathForm.CORPSE)) {
            ragdoller.ragdoll(event.limb)
            val oldLimbs = event.limb.getDescendants()
            for (limb in event.limb.getDescendants(includeInactive = true)) {
                val oldEntity = oldLimbs.first { it.name == limb.name }.entity
                createCorpseLimb(corpseLiveTime, limb.entity, oldEntity)
            }
        }
    }

    private fun createCorpseLimb(corpseLiveTime: Float, corpseEntity: Entity, oldEntity: Entity) {
        val transform = corpseEntity[TransformPart::class].transform
        if (transform is Box2dTransform) {
            Box2dUtils.setFilter(transform.body, mask = CollisionCategory.STATIC)
        }
        val spritePart = oldEntity.tryGet(SpritePart::class)
        if (spritePart != null) {
            corpseEntity.attach(spritePart)
        }
        corpseEntity.attach(TimedDeathPart(corpseLiveTime))
        entityManager.add(corpseEntity)
    }
}