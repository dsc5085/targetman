package dc.targetman.graphics

import com.badlogic.gdx.graphics.Camera
import dc.targetman.mechanics.Alliance
import dc.targetman.mechanics.EntityFinder
import dc.targetman.system.InputUtils
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
            val muzzlePosition = player[TransformPart::class].transform.center
            val cursorWorldCoord = InputUtils.getCursorWorldCoord(screenHelper)
            val cameraOffset = VectorUtils.offset(muzzlePosition, cursorWorldCoord).scl(0.5f)
            val cameraPosition = muzzlePosition.cpy().add(cameraOffset)
            CameraUtils.lookAt(cameraPosition, screenHelper, camera)
        }
    }
}