package dc.targetman.ai

/**
 * Contains current AI state related to reaching a node.
 */
class SteerState {
    var climbState = ClimbState.MOVE_TO_LADDER

    fun reset() {
        climbState = ClimbState.MOVE_TO_LADDER
    }
}