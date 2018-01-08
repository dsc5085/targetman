package dc.targetman.ai

/**
 * Contains current AI state related to reaching a node.
 */
class SteerState {
    var dismounted = false

    fun reset() {
        dismounted = false
    }
}