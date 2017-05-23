package dc.targetman.command

interface Executer {
    val verb: String
    fun execute(params: Map<String, String>)
}