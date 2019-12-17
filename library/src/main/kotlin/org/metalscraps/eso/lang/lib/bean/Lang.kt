package org.metalscraps.eso.lang.lib.bean

import org.metalscraps.eso.lang.lib.config.AppVariables

class Lang(file: String, val writeFileName: Boolean, val beta: Boolean) {
    val file = AppVariables.workDir.resolve(file)
}

