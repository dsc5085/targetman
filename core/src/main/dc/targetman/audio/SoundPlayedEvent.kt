package dc.targetman.audio

import com.badlogic.gdx.math.Vector2
import dclib.epf.Entity

data class SoundPlayedEvent(val origin: Vector2, val range: Float, val entity: Entity)