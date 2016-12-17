package dc.targetman.character

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Skeleton

val Skeleton.bounds: Rectangle
    get() {
        val offset = Vector2()
        val size = Vector2()
        getBounds(offset, size)
        return Rectangle(offset.x, offset.y, size.x, size.y)
    }