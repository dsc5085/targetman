package dc.targetman.physics

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import dclib.geometry.PolygonUtils
import dclib.geometry.base
import dclib.geometry.size
import dclib.physics.Box2dUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class JumpCheckerTest {
    private val GRAVITY = -9.8f
    private val AGENT_SPEED = Vector2(10f, 10f)

    @Test
    fun isValid_Up_True() {
        val map = arrayOf(
                "#",
                "#   #",
                "#",
                "#",
                "##"
        )
        val from = Vector2(1.5f, 1f)
        testIsValid(true, map, from, from.cpy().add(2f, 3f))
    }

    @Test
    fun isValid_Drop_True() {
        val map = arrayOf(
                "#",
                "#   #",
                "#",
                "#",
                "#"
        )
        val from = Vector2(3.5f, 4f)
        testIsValid(true, map, from, from.cpy().add(0f, -4f))
    }

    @Test
    fun isValid_Far_True() {
        val map = arrayOf(
                " #",
                "##"
        )
        val from = Vector2(0.5f, 1f)
        testIsValid(true, map, from, from.cpy().add(-16f, -5f))
    }

    @Test
    fun isValid_CloseBlocked_False() {
        val map = arrayOf(
                "#",
                "##"
        )
        val from = Vector2(1.5f, 1f)
        testIsValid(false, map, from, from.cpy().add(-3f, 3f))
    }

    @Test
    fun isValid_FarBlocked_False() {
        val map = arrayOf(
                "    #",
                "    #",
                "    #",
                "    #",
                "##  ###"
        )
        val from = Vector2(1.5f, 1f)
        testIsValid(false, map, from, from.cpy().add(4f, 0f))
    }

    @Test
    fun isValid_InWall_False() {
        val map = arrayOf(
                "##",
                "##"
        )
        val from = Vector2(1.5f, 1f)
        testIsValid(false, map, from, from.cpy().add(3f, 3f))
    }

    @Test
    fun isValid_TooFar_False() {
        val map = arrayOf(
                "#",
                "##"
        )
        val from = Vector2(1.5f, 1f)
        testIsValid(false, map, from, from.cpy().add(20f, 3f))
    }

    @Test
    fun isValid_FloorBlocking_False() {
        val map = arrayOf(
                "#",
                "###"
        )
        val from = Vector2(1f, 1f)
        testIsValid(false, map, from, from.cpy().add(0f, -1f))
    }

    private fun testIsValid(expected: Boolean, map: Array<String>, from: Vector2, to: Vector2) {
        val world = createWorld(map)
        val jumpChecker = JumpChecker(world, AGENT_SPEED)
        val bounds = Rectangle(0f, 0f, 1f, 1f)
        val isValid = jumpChecker.isValid(from, to, bounds.size, bounds.base)
        world.dispose()
        assertEquals(expected, isValid)
    }

    private fun createWorld(map: Array<String>): World {
        val world = World(Vector2(0f, GRAVITY), true)
        val reversedMap = map.reversedArray()
        for (i in 0 until reversedMap.size) {
            for (j in 0 until reversedMap[i].length) {
                val char = reversedMap[i][j]
                if (char == '#') {
                    createWall(world, j.toFloat(), i.toFloat())
                }
            }
        }
        return world
    }

    private fun createWall(world: World, x: Float, y: Float) {
        val vertices = PolygonUtils.createRectangleVertices(Rectangle(x, y, 1f, 1f))
        Box2dUtils.createStaticBody(world, vertices)
    }
}