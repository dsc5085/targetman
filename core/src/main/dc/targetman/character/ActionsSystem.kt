package dc.targetman.character

import dc.targetman.mechanics.ActionsPart
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.EntitySystem

class ActionsSystem(entityManager: EntityManager) : EntitySystem(entityManager) {
    override fun update(delta: Float, entity: Entity) {
        val actionsPart = entity.tryGet(ActionsPart::class)
        if (actionsPart != null) {
            for (action in actionsPart.actions) {
                action.wasDoing = action.doing
                action.doing = false
            }
        }
    }
}