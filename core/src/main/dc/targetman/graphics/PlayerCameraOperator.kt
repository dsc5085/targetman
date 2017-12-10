package dc.targetman.graphics

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityFinder
import dc.targetman.system.InputUtils
import dclib.epf.Entity
import dclib.epf.EntityManager
import dclib.epf.parts.TransformPart
import dclib.geometry.VectorUtils
import dclib.graphics.CameraUtils
import dclib.graphics.ScreenHelper
import dclib.system.Updater

class PlayerCameraOperator(
        private val camera: Camera,
        private val screenHelper: ScreenHelper,
        private val entityManager: EntityManager)
    : Updater {
    override fun update(delta: Float) {
        val player = EntityFinder.find(entityManager, Alliance.PLAYER)
        if (player != null) {
            val maxCameraSpeed = 10f
            val viewportWorldSize = screenHelper.toWorldUnits(camera.viewportWidth, camera.viewportHeight)
            val maxCameraDistance = Math.hypot(viewportWorldSize.x.toDouble(), viewportWorldSize.y.toDouble())
                    .toFloat()
//            val maxCameraDistance = VectorUtils.offset(player[TransformPart::class].transform.center, InputUtils.getCursorWorldCoord(screenHelper)).len() / 2f
            val lookAtTarget = getLookAtTarget(player)
//            println(lookAtTarget)
            val cameraCenter = CameraUtils.center(camera, screenHelper)
            val offsetToLookAtTarget = VectorUtils.offset(cameraCenter, lookAtTarget)
//            println(offsetToLookAtTarget)
            val cameraSpeed = Interpolation.exp10Out.apply(0f, maxCameraSpeed * delta, 1f - offsetToLookAtTarget.len() / maxCameraDistance)
//            println(cameraSpeed)
//            val currentOffsetLength = VectorUtils.offset(CameraUtils.center(camera, screenHelper), destOffset).len()
//            val progress = currentOffsetLength / destOffset.len() //(maxCameraDistance - destOffset.len()) / maxCameraDistance
//            val cameraSpeed = Interpolation.exp10Out.apply(0f, maxCameraSpeed * delta, 1f - progress)
            val currentOffset = offsetToLookAtTarget.cpy().setLength(cameraSpeed)
            val newCameraPosition = CameraUtils.center(camera, screenHelper).add(currentOffset)
            println(cameraCenter)
            CameraUtils.lookAt(newCameraPosition, screenHelper, camera)
        }
    }

    private fun getLookAtTarget(player: Entity): Vector2 {
        val playerPosition = player[TransformPart::class].transform.center
        val cursorWorldCoord = InputUtils.getCursorWorldCoord(screenHelper)
        return playerPosition.cpy().interpolate(cursorWorldCoord, 0.5f, Interpolation.linear)
    }
}