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
import dclib.geometry.center
import dclib.physics.collision.CollisionChecker

class AiSystem(
        private val entityManager: EntityManager,
        private val steering: Steering,
        private val pathUpdater: PathUpdater,
        private val collisionChecker: CollisionChecker
) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val aiPart = entity.tryGet(AiPart::class)
        if (aiPart != null) {
            aiPart.tick(delta)
            val target = EntityFinder.find(entityManager, Alliance.PLAYER)
            if (target != null) {
                val agent = DefaultAgent(entity, target)
                steer(agent)
                if (aiPart.checkDetect()) {
                    detectTarget(agent, aiPart)
                }
                if (aiPart.isAlert) {
                    pathUpdater.update(agent)
                    aim(entity, agent.targetBounds)
                    CharacterActions.trigger(entity)
                }
            }
        }
    }

    private fun steer(agent: Agent) {
        if (agent.path.isNotEmpty) {
            steering.update(agent)
        }
    }

    private fun detectTarget(agent: Agent, aiPart: AiPart) {
        if (AiUtils.isTargetInSight(agent, collisionChecker)) {
            aiPart.resetAlertTimer()
        }
    }

    private fun aim(entity: Entity, targetBounds: Rectangle) {
        val muzzleName = entity[FiringPart::class].muzzleName
        val muzzle = entity[SkeletonPart::class].tryGet(muzzleName)
        if (muzzle != null) {
            CharacterActions.aim(entity, targetBounds.center)
        }
    }
}
