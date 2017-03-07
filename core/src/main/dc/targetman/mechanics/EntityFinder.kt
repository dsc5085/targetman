package dc.targetman.mechanics

import dc.targetman.epf.parts.MovementPart
import dclib.epf.Entity
import dclib.epf.EntityManager

object EntityFinder {
    fun find(entityManager: EntityManager, attribute: Enum<*>): Entity? {
        return entityManager.getAll().firstOrNull {
            it.has(MovementPart::class) && it.of(attribute)
        }
    }
}
