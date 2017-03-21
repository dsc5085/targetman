package dc.targetman.skeleton

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.Slot
import dc.targetman.util.ArrayUtils

fun Skeleton.getBounds(): Rectangle {
    return getBounds(drawOrder.toList())
}

fun Skeleton.getBounds(includedSlots: Collection<Slot>): Rectangle {
    val offset = Vector2()
    val size = Vector2()
    val oldDrawOrder = drawOrder
    drawOrder = ArrayUtils.toGdxArray(includedSlots)
    getBounds(offset, size)
    drawOrder = oldDrawOrder
    return Rectangle(offset.x, offset.y, size.x, size.y)
}