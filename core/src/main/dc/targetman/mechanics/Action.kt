package dc.targetman.mechanics

class Action(val key: Enum<*>, var isExecuting: Boolean, var wasExecuting: Boolean) {
    val justExecuted get() = isExecuting && !wasExecuting
}