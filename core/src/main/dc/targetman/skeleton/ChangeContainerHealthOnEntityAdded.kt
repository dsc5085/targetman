package dc.targetman.skeleton

import dclib.epf.Entity
import dclib.epf.EntityAddedEvent
import dclib.epf.EntityManager
import dclib.epf.parts.HealthPart
import dclib.mechanics.HealthChangedEvent

class ChangeContainerHealthOnEntityAdded(private val entityManager: EntityManager) : (EntityAddedEvent) -> Unit {
    override fun invoke(event: EntityAddedEvent) {
        val healthPart = event.entity.tryGet(HealthPart::class)
        if (healthPart != null) {
            healthPart.healthChanged.on { handleHealthChanged(event.entity, it) }
        }
    }

    private fun handleHealthChanged(entity: Entity, event: HealthChangedEvent) {
        val container = LimbUtils.findContainer(entityManager.all, entity)
        if (container != null && container != entity) {
            container.tryGet(HealthPart::class)?.change(event.change)
        }
    }
}