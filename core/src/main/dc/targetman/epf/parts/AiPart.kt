package dc.targetman.epf.parts

import com.badlogic.gdx.math.MathUtils
import dc.targetman.ai.AiProfile
import dc.targetman.ai.Path
import dc.targetman.ai.SteerState
import dclib.util.Timer

class AiPart(val profile: AiProfile) {
    private val CALCULATE_PATH_TIME = MathUtils.random(0.07f, 0.12f)
    private val DETECT_TIME = MathUtils.random(0.01f, 0.015f)

    val path = Path()
    val steerState = SteerState()
    val waitTimer: Timer = Timer()
    val alertTimer = Timer(10f, 10f)
    val sightTimer = Timer(2f, 2f)

    private val calculatePathTimer = Timer(CALCULATE_PATH_TIME, CALCULATE_PATH_TIME)
    private val detectTimer = Timer(DETECT_TIME, DETECT_TIME)

    fun checkCalculatePath(): Boolean {
        return calculatePathTimer.check()
    }

    fun checkDetect(): Boolean {
        return detectTimer.check()
    }

    fun resetAlertTimer() {
        alertTimer.reset()
    }

    fun tick(delta: Float) {
        calculatePathTimer.tick(delta)
        detectTimer.tick(delta)
        waitTimer.tick(delta)
        alertTimer.tick(delta)
        sightTimer.tick(delta)
    }
}
