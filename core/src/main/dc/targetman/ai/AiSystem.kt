package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityFinder
import dc.targetman.mechanics.character.CharacterActions
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.geometry.center

// TODO: Combine with InputUpdater.  Pipe actions into InputUpdater
class AiSystem(
        private val entityManager: EntityManager,
        private val steering: Steering,
        private val pathUpdater: PathUpdater
) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val aiPart = entity.tryGet(AiPart::class)
        if (aiPart != null) {
            aiPart.tick(delta)
            val target = EntityFinder.find(entityManager, Alliance.PLAYER)
            if (target != null) {
                val targetBounds = target[TransformPart::class].transform.bounds
                move(entity, targetBounds)
                aim(entity, targetBounds)
                CharacterActions.trigger(entity)
            }
        }
    }

    private fun move(entity: Entity, targetBounds: Rectangle) {
        val agent = Agent(entity, targetBounds)
        steering.seek(agent)
        pathUpdater.update(agent)
    }

    private fun aim(entity: Entity, targetBounds: Rectangle) {
        val skeletonPart = entity.get(SkeletonPart::class)
        val muzzleName = entity[FiringPart::class].muzzleName
        val muzzle = skeletonPart.tryGet(muzzleName)
        if (muzzle != null) {
            CharacterActions.aim(entity, targetBounds.center)
        }
    }
}
