package dc.targetman.ai

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import dc.targetman.ai.graph.ConnectionType
import dc.targetman.ai.graph.DefaultConnection
import dc.targetman.ai.graph.DefaultNode
import dc.targetman.ai.graph.GraphQuery
import dc.targetman.ai.graph.Segment
import dc.targetman.mechanics.Direction
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class SteeringTest {
    private val GRAVITY = -9.8f

    // This group of segments forms a pyramid structure
    private val UPPER_SEGMENT = Segment(Rectangle(2f, 5f, 5f, 1f))
    private val LOWER_SEGMENT = Segment(Rectangle(1f, 1f, 8f, 1f))

    @Test
    fun update_LeftEdgeDrop_MoveLeft() {
        val fromNode = UPPER_SEGMENT.leftNode
        val toNode = LOWER_SEGMENT.createNode(UPPER_SEGMENT.leftNode.x)
        val position = Vector2(UPPER_SEGMENT.left - 0.5f, UPPER_SEGMENT.y)
        val agent = createAgent(position, fromNode, toNode)
        steer(agent, listOf(UPPER_SEGMENT, LOWER_SEGMENT))
        verify(agent).moveHorizontal(Direction.LEFT)
    }

    @Test
    fun update_RightEdgeDrop_MoveRight() {
        val fromNode = UPPER_SEGMENT.rightNode
        val toNode = LOWER_SEGMENT.createNode(UPPER_SEGMENT.right)
        val position = Vector2(UPPER_SEGMENT.right - 0.5f, LOWER_SEGMENT.y)
        val agent = createAgent(position, fromNode, toNode)
        steer(agent, listOf(UPPER_SEGMENT, LOWER_SEGMENT))
        verify(agent).moveHorizontal(Direction.RIGHT)
    }

    @Test
    fun update_OutsideVerticalHopToLeftEdge_MoveRight() {
        val fromNode = LOWER_SEGMENT.createNode(UPPER_SEGMENT.leftNode.x)
        val toNode = UPPER_SEGMENT.leftNode
        val position = Vector2(0.5f, LOWER_SEGMENT.y)
        val agent = createAgent(position, fromNode, toNode)
        steer(agent, listOf(LOWER_SEGMENT, UPPER_SEGMENT))
        verify(agent).moveHorizontal(Direction.RIGHT)
    }

    @Test
    fun update_OutsideVerticalHopToRightEdge_MoveLeft() {
        val fromNode = LOWER_SEGMENT.createNode(UPPER_SEGMENT.rightNode.x)
        val toNode = UPPER_SEGMENT.rightNode
        val position = Vector2(LOWER_SEGMENT.right - 0.5f, LOWER_SEGMENT.y)
        val agent = createAgent(position, fromNode, toNode)
        steer(agent, listOf(LOWER_SEGMENT, UPPER_SEGMENT))
        verify(agent).moveHorizontal(Direction.LEFT)
    }

    @Test
    fun update_InsideVerticalHopToLeftEdge_MoveLeft() {
        val fromNode = LOWER_SEGMENT.createNode(UPPER_SEGMENT.leftNode.x)
        val toNode = UPPER_SEGMENT.leftNode
        val position = Vector2(UPPER_SEGMENT.left - 0.5f, LOWER_SEGMENT.y)
        val agent = createAgent(position, fromNode, toNode)
        steer(agent, listOf(LOWER_SEGMENT, UPPER_SEGMENT))
        verify(agent).moveHorizontal(Direction.LEFT)
    }

    @Test
    fun update_InsideVerticalHopToRightEdge_MoveRight() {
        val fromNode = LOWER_SEGMENT.createNode(UPPER_SEGMENT.rightNode.x)
        val toNode = UPPER_SEGMENT.rightNode
        val position = Vector2(UPPER_SEGMENT.right - 0.5f, LOWER_SEGMENT.y)
        val agent = createAgent(position, fromNode, toNode)
        steer(agent, listOf(LOWER_SEGMENT, UPPER_SEGMENT))
        verify(agent).moveHorizontal(Direction.RIGHT)
    }

    // After dismounting, make sure AI keeps moving to toNode

    @Test
    fun update_Falling_DontTryToJump() {
        val fromNode = UPPER_SEGMENT.leftNode
        val toNode = LOWER_SEGMENT.createNode(UPPER_SEGMENT.left)
        val position = Vector2(UPPER_SEGMENT.left, UPPER_SEGMENT.y + 1f)
        val velocity = Vector2(0f, -0.1f)
        val agent = createAgent(position, fromNode, toNode, velocity)
        steer(agent, listOf(UPPER_SEGMENT, LOWER_SEGMENT))
        verify(agent, never()).jump()
    }

    fun steer(agent: Agent, segments: List<Segment>) {
        val graphQuery = mock(GraphQuery::class.java)
        for (segment in segments) {
            for (node in segment.getNodes()) {
                `when`(graphQuery.getSegment(node)).thenReturn(segments.single { it.getNodes().contains(node) })
            }
        }
        Steering(graphQuery, GRAVITY).update(agent)
    }

    fun createAgent(position: Vector2, fromNode: DefaultNode, toNode: DefaultNode, velocity: Vector2 = Vector2(0f, 0f)): Agent {
        val agent = mock(Agent::class.java)
        val path = Path()
        path.set(listOf(DefaultConnection(fromNode, toNode, ConnectionType.NORMAL)))
        `when`(agent.path).thenReturn(path)
        `when`(agent.speed).thenReturn(Vector2(5f, 3f))
        `when`(agent.velocity).thenReturn(velocity)
        `when`(agent.bounds).thenReturn(Rectangle(position.x, position.y, 1f, 1f))
        return agent
    }
}