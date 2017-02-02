package dc.targetman.mechanics.weapon

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import dc.targetman.skeleton.SkeletonUtils
import dc.targetman.skeleton.bounds
import dclib.geometry.size
import dclib.util.FloatRange
import dclib.util.Timer

class Weapon(val data: WeaponData, atlas: TextureAtlas) {
    val reloadTimer = Timer(data.reloadTime, data.reloadTime)
    val speedRange = FloatRange(data.minSpeed, data.maxSpeed)
    val skeleton = SkeletonUtils.createSkeleton(data.skeletonPath, atlas)

    val size: Vector2
        get() {
            val size = skeleton.bounds.size
            val weaponHeight = data.width * size.y / size.x
            return Vector2(data.width, weaponHeight)
        }
}