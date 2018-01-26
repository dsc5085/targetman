package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import dc.targetman.audio.SoundManager
import dc.targetman.audio.SoundPlayedEvent
import dc.targetman.epf.parts.AiPart
import dc.targetman.epf.parts.FiringPart
import dc.targetman.epf.parts.MovementPart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityFinder
import dc.targetman.mechanics.EntityUtils
import dc.targetman.mechanics.character.CharacterActions
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.epf.parts.TransformPart
import dclib.geometry.center
import dclib.physics.collision.CollidedEvent
import dclib.physics.collision.CollisionChecker

class AiSystem(
        private val entityManager: EntityManager,
        private val steering: Steering,
        private val pathUpdater: PathUpdater,
        private val collisionChecker: CollisionChecker,
        soundManager: SoundManager
) : EntitySystem(entityManager) {
    init {
        collisionChecker.collided.on(this::handleCollided)
        soundManager.played.on(this::handleSoundPlayed)
    }

    override fun update(delta: Float, entity: Entity) {
        val aiPart = entity.tryGet(AiPart::class)
        if (aiPart != null) {
            aiPart.tick(delta)
            val target = EntityFinder.find(entityManager, Alliance.PLAYER)
            if (target != null) {
                val agent = DefaultAgent(entity, target)
                if (aiPart.checkDetect()) {
                    detectTarget(agent, aiPart)
                }
                pathUpdater.update(agent)
                if (aiPart.waitTimer.isElapsed) {
                    steer(agent)
                }
                val movementPart = entity[MovementPart::class]
                if (aiPart.isAlert) {
                    aim(entity, agent.targetBounds)
                    CharacterActions.trigger(entity)
                    movementPart.runSpeedRatio = 1f
                } else {
                    movementPart.runSpeedRatio = MovementPart.WALK_SPEED_RATIO
                }
            }
        }
    }

    private fun steer(agent: Agent) {
        if (!agent.path.isEmpty) {
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

    private fun handleCollided(event: CollidedEvent) {
        val aiPart = event.collision.source.entity.tryGet(AiPart::class)
        if (aiPart != null) {
            if (EntityUtils.areOpposing(event.collision.source.entity, event.collision.target.entity)) {
                aiPart.resetAlertTimer()
            }
        }
    }

    private fun handleSoundPlayed(event: SoundPlayedEvent) {
        for (entity in entityManager.getAll()) {
            val aiPart = entity.tryGet(AiPart::class)
            if (aiPart != null && EntityUtils.areOpposing(entity, event.entity)) {
                val center = entity[TransformPart::class].transform.center
                if (center.cpy().dst(event.origin) <= event.range) {
                    aiPart.resetAlertTimer()
                }
            }
        }
    }
}
