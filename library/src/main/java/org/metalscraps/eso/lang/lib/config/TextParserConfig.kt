package org.metalscraps.eso.lang.lib.config

import org.metalscraps.eso.lang.lib.util.KUtils
import java.nio.file.Path
import java.util.regex.Pattern

/**
 * Created by 안병길 on 2018-01-20.
 * Whya5448@gmail.com
 */

@Deprecated("컨피그 안씀")
class TextParserConfig {

    var path: Path? = null
        set(path) : Unit {
            field = path
            autoPattern()
        }
    val keyGroup = 2
    var pattern: Pattern? = null

    private fun autoPattern() {
        if (pattern == null) {
            val ext = KUtils.getExtension(this.path!!)
            if (ext == "po" || ext == "po2") pattern = AppConfig.POPattern
            else if (ext == "csv") pattern = AppConfig.CSVPattern
        }
    }
}
