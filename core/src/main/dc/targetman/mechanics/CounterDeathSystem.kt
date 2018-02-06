package dc.targetman.mechanics

import dc.targetman.epf.parts.CounterDeathPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem

class CounterDeathSystem(private val entityManager: EntityManager)
    : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val counterDeathPart = entity.tryGet(CounterDeathPart::class)
        if (counterDeathPart != null) {
            counterDeathPart.counter--
            if (counterDeathPart.counter <= 0) {
                entityManager.destroy(entity)
            }
        }
    }
}