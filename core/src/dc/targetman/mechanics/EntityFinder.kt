package dc.targetman.mechanics

import dc.targetman.epf.parts.MovementPart
import dclib.epf.Entity
import dclib.epf.EntityManager

object EntityFinder {
    fun findPlayer(entityManager: EntityManager): Entity? {
        return entityManager.all.firstOrNull {
            it.has(MovementPart::class.java) && it.of(Alliance.PLAYER)
        }
    }
}
