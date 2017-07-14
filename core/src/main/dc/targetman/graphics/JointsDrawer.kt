package dc.targetman.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Joint
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import dclib.graphics.ScreenHelper

class JointsDrawer(
        private val world: World,
        private val shapeRenderer: ShapeRenderer,
        private val screenHelper: ScreenHelper
)  {
    fun draw() {
        screenHelper.setScaledProjectionMatrix(shapeRenderer)
        shapeRenderer.color = Color.PINK
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val joints = Array<Joint>()
        world.getJoints(joints)
        for (joint in joints) {
            shapeRenderer.circle(joint.anchorA.x, joint.anchorA.y, 0.03f, 8)
        }
        shapeRenderer.end()
    }
}