package dc.targetman.audio

import dclib.eventing.EventDelegate

class SoundManager {
    val soundEvent = EventDelegate<SoundEvent>()
}