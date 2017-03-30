package dc.targetman.mechanics

import dc.targetman.epf.parts.ForcePart
import dc.targetman.epf.parts.SkeletonPart
import dc.targetman.epf.parts.StaggerPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem
import dclib.physics.collision.CollidedEvent
import dclib.physics.collision.CollisionChecker

class StaggerSystem(entityManager: EntityManager, collisionChecker: CollisionChecker) : EntitySystem(entityManager) {
    init {
        collisionChecker.collided.on { handleCollided(it) }
    }

    override fun update(delta: Float, entity: Entity) {
        val staggerPart = entity.tryGet(StaggerPart::class)
        if (staggerPart != null) {
            if (staggerPart.isStaggered) {
                staggerPart.staggerTimer.tick(delta)
                if (staggerPart.staggerTimer.check()) {
                    staggerPart.isStaggered = false
                    // turn limbs back into skeleton limbs
                    // animate to standing back up
                }
            }
        }
    }

    private fun handleCollided(event: CollidedEvent) {
        val forcePart = event.source.entity.tryGet(ForcePart::class)
        val staggerPart = event.target.entity.tryGet(StaggerPart::class)
        if (forcePart != null && staggerPart != null) {
            if (forcePart.force >= staggerPart.minForce) {
                staggerPart.isStaggered = true
                ragdoll(event.target.entity)
            }
        }
    }

    private fun ragdoll(entity: Entity) {
        // turn limbs into ragdoll
        // disable skeletonPart
        val skeletonPart = entity[SkeletonPart::class]
    }
}