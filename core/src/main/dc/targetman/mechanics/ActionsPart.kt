package dc.targetman.mechanics

class ActionsPart(val actions: MutableSet<Action> = mutableSetOf()) {
    operator fun get(key: Enum<*>): Action {
        var action = actions.firstOrNull { it.key == key }
        if (action == null) {
            action = Action(key, false)
            actions.add(action)
        }
        return action
    }
}