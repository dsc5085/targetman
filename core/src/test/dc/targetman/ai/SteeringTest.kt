package dc.targetman.ai

import org.junit.Test
import org.mockito.Mockito.mock

class SteeringTest {
    @Test
    fun seek_SameSegmentAsTarget_MovesToTarget() {
        val graphHelper = mock(GraphHelper::class.java)
        val steering = Steering(graphHelper)
    }

    @Test
    fun seek_DifferentSegmentAsTarget_MovesToNextNode() {
    }

    @Test
    fun seek_CannotGetToTarget_DoesNotMove() {
    }

    @Test
    fun seek_MustJumpToNextNode_Jumps() {
    }

    @Test
    fun seek_CanDropToNextNode_DoesNotJump() {
    }

    @Test
    fun seek_NextNodeOnSameSegment_DoesNotJump() {
    }

    @Test
    fun seek_ApproachingEdge_Jumps() {
    }

    @Test
    fun seek_FacingTargetInRange_DoesNotMove() {
    }

    @Test
    fun seek_NotFacingTargetInRange_MovesToTarget() {
    }
}