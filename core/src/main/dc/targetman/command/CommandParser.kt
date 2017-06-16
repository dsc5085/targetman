package dc.targetman.command

class CommandParser {
    fun parseCommand(text: String): Command {
        val paramToken = '-'
        val verbParseResult = parse(text, paramToken, canSkipEndToken = true)
        val params = mutableMapOf<String, String>()
        var remainingText = verbParseResult.remaining
        do {
            val parsedField = parse(remainingText, ' ', paramToken)
            remainingText = if (parsedField.isParsed) addNextParam(params, parsedField) else ""
        } while(remainingText.isNotBlank())
        return Command(verbParseResult.parsed, params)
    }

    private fun addNextParam(params: MutableMap<String, String>, parsedField: ParseResult): String {
        val remainingText = parsedField.remaining
        val parsedValue = parseParamValue(remainingText)
        params.put(parsedField.parsed, parsedValue.parsed)
        return parsedValue.remaining
    }

    private fun parseParamValue(remainingText: String): ParseResult {
        val literalToken = '"'
        val parsedValue: ParseResult
        if (remainingText.startsWith(literalToken)) {
            parsedValue = parse(remainingText, literalToken, literalToken)
        } else {
            parsedValue = parse(remainingText)
        }
        return parsedValue
    }

    private fun parse(text: String, endToken: Char? = null, startToken: Char? = null, canSkipEndToken: Boolean = false)
            : ParseResult {
        val startIndex = if (startToken == null) 0 else text.indexOf(startToken)
        var endIndex = if (endToken == null) text.length else text.indexOf(endToken, startIndex + 1)
        if (endIndex < 0 && canSkipEndToken) {
            endIndex = text.length
        }
        val foundTokens = startIndex >= 0 && endIndex >= 0
        return if (foundTokens) parse(endIndex, startIndex, startToken, text) else ParseResult(false, "", "")
    }

    private fun parse(endIndex: Int, startIndex: Int, startToken: Char?, text: String): ParseResult {
        val remaining = text.substring(endIndex).trim()
        var parsed = text.substring(startIndex, endIndex).trim()
        if (startToken != null) {
            parsed = parsed.trimStart(startToken)
        }
        return ParseResult(true, remaining, parsed)
    }

    private data class ParseResult(val isParsed: Boolean, val remaining: String, val parsed: String)
}