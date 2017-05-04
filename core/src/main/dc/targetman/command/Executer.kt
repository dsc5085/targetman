package dc.targetman.command

interface Executer<T> {
    val verb: String
    fun execute(params: Map<String, String>)
}