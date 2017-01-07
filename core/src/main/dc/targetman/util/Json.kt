package dc.targetman.util

import com.badlogic.gdx.files.FileHandle
import dclib.system.io.FileUtils
import java.util.regex.Pattern

object Json {
    /**
     * @return the serialized object.  This function automatically resolves links to other json files,
     * e.g. "address": "addresses/delivery_address.json"
     */
    inline fun <reified T : Any> toObject(file: FileHandle): T {
        val json = com.badlogic.gdx.utils.Json()
        val jsonString = toString(file)
        return json.fromJson(T::class.java, jsonString)
    }

    fun toString(file: FileHandle): String {
        var string = file.readString()
        val jsonFileNameRegex = "\"[^\"]*\\.json\""
        val matcher = Pattern.compile(jsonFileNameRegex).matcher(string)
        while (matcher.find()) {
            val internalPath = matcher.group().removeSurrounding("\"")
            val childFile = FileUtils.toFileHandle(internalPath)
            val childString = toString(childFile)
            string = matcher.replaceFirst(childString)
        }
        return string
    }
}