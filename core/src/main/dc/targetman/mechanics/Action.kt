package dc.targetman.mechanics

class Action(val key: Enum<*>, var doing: Boolean, var wasDoing: Boolean) {
    val justDid get() = doing && !wasDoing
}