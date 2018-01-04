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

    @Test
    fun update_LeftDrop_MoveLeft() {
        val graphQuery = mock(GraphQuery::class.java)
        val steering = Steering(graphQuery, GRAVITY)
        val fromSegment = Segment(Rectangle(2f, 5f, 5f, 1f))
        val toSegment = Segment(Rectangle(2f, 1f, 8f, 1f))
        val fromNode = fromSegment.leftNode
        val toNode = DefaultNode(2f, 1f)
        toSegment.add(toNode)
        `when`(graphQuery.getSegment(fromNode)).thenReturn(fromSegment)
        `when`(graphQuery.getSegment(toNode)).thenReturn(toSegment)
        val agent = createAgent(listOf(DefaultConnection(fromNode, toNode, ConnectionType.NORMAL)))
        steering.update(agent)
        verify(agent).moveHorizontal(Direction.LEFT)
    }

    // TODO: Consolidate test code
    @Test
    fun update_Falling_DontTryToJump() {
        val graphQuery = mock(GraphQuery::class.java)
        val steering = Steering(graphQuery, GRAVITY)
        val fromSegment = Segment(Rectangle(2f, 5f, 5f, 1f))
        val toSegment = Segment(Rectangle(2f, 1f, 8f, 1f))
        val fromNode = fromSegment.leftNode
        val toNode = DefaultNode(2f, 1f)
        toSegment.add(toNode)
        `when`(graphQuery.getSegment(fromNode)).thenReturn(fromSegment)
        `when`(graphQuery.getSegment(toNode)).thenReturn(toSegment)
        val agent = createAgent(listOf(DefaultConnection(fromNode, toNode, ConnectionType.NORMAL)), Vector2(0f, -0.1f))
        steering.update(agent)
        verify(agent, never()).jump()
    }

    fun createAgent(pathConnections: List<DefaultConnection>, velocity: Vector2 = Vector2(0f, 0f)): Agent {
        val agent = mock(Agent::class.java)
        val path = Path()
        path.set(pathConnections)
        `when`(agent.path).thenReturn(path)
        `when`(agent.speed).thenReturn(Vector2(5f, 3f))
        `when`(agent.velocity).thenReturn(velocity)
        `when`(agent.bounds).thenReturn(Rectangle(2f, 4.5f, 1f, 1f))
        return agent
    }
}