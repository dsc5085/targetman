package dc.targetman.character

import com.badlogic.gdx.physics.box2d.World
import dc.targetman.physics.collision.CollisionCategory
import dc.targetman.skeleton.LimbBranchRemovedEvent
import dc.targetman.skeleton.Ragdoller
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.SpritePart
import dclib.epf.parts.TimedDeathPart
import dclib.epf.parts.TransformPart
import dclib.physics.Box2dTransform
import dclib.physics.Box2dUtils

class CorpseOnLimbRemoved(val entityManager: EntityManager, val world: World) : (LimbBranchRemovedEvent) -> Unit {
    override fun invoke(event: LimbBranchRemovedEvent) {
        val corpseLiveTime = 30f
        if (event.rootLimb.entity.of(DeathForm.CORPSE)) {
            Ragdoller.ragdoll(event.rootLimb)
            for (limb in event.rootLimb.getDescendants()) {
                createCorpseEntity(limb.entity, corpseLiveTime)
            }
        }
    }

    private fun createCorpseEntity(removedEntity: Entity, corpseLiveTime: Float) {
        val corpseEntity = Entity()
        val transform = removedEntity[TransformPart::class].transform
        if (transform is Box2dTransform) {
            Box2dUtils.setFilter(transform.body, mask = CollisionCategory.STATIC)
        }
        corpseEntity.attach(TransformPart(transform))
        val spritePart = removedEntity.tryGet(SpritePart::class)
        if (spritePart != null) {
            corpseEntity.attach(spritePart)
        }
        corpseEntity.attach(TimedDeathPart(corpseLiveTime))
        entityManager.add(corpseEntity)
    }
}