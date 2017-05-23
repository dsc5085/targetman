package dc.targetman.command

import dclib.eventing.DefaultEvent
import dclib.eventing.EventDelegate

class CommandModule(internal val executers: Collection<Executer>) {
    internal val disposed = EventDelegate<DefaultEvent>()

    fun dispose() {
        disposed.notify(DefaultEvent())
    }
}
