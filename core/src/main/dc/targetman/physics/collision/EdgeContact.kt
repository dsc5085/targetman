package dc.targetman.physics.collision

import com.badlogic.gdx.math.Vector2
import dclib.geometry.Segment2

data class EdgeContact(val edge: Segment2, val intersection: Vector2)