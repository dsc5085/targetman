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
        private val entityManager: EntityManager
) : Updater {
    override fun update(delta: Float) {
        val maxCameraSpeed = 20f
        val player = EntityFinder.find(entityManager, Alliance.PLAYER)
        if (player != null) {
            val viewportWorldSize = screenHelper.toWorldUnits(camera.viewportWidth, camera.viewportHeight)
            val maxCameraDistance = Math.hypot(viewportWorldSize.x.toDouble(), viewportWorldSize.y.toDouble())
                    .toFloat()
            val cameraDestination = getCameraDestination(player)
            val cameraCenter = screenHelper.toWorldUnits(camera.position.x, camera.position.y)
            val offsetToDestination = VectorUtils.offset(cameraCenter, cameraDestination)
            val progress = offsetToDestination.len() / maxCameraDistance
            val cameraSpeed = Interpolation.exp10Out.apply(0f, maxCameraSpeed, progress)
            val frameOffset = offsetToDestination.cpy().setLength(cameraSpeed * delta)
            val newCameraPosition = cameraCenter.cpy().add(frameOffset)
            CameraUtils.lookAt(newCameraPosition, screenHelper, camera)
        }
    }

    private fun getCameraDestination(player: Entity): Vector2 {
        val playerPosition = player[TransformPart::class].transform.center
        val cursorWorldCoord = InputUtils.getCursorWorldCoord(screenHelper)
        return playerPosition.cpy().interpolate(cursorWorldCoord, 0.5f, Interpolation.linear)
    }
}