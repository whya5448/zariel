package org.metalscraps.eso.lang.lib.bean

import org.metalscraps.eso.lang.lib.config.AppVariables
import java.nio.file.Path

class Lang(private val fileName: String, val writeFileName: Boolean, val beta: Boolean) {
    val file: Path
        get() {
            return AppVariables.workDir.resolve(fileName)
        }
}

