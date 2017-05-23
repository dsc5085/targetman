package dc.targetman.command

data class Command(val verb: String, val params: Map<String, String>)