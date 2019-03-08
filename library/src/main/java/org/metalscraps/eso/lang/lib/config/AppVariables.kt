package org.metalscraps.eso.lang.lib.config

import org.metalscraps.eso.lang.lib.util.Utils
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Created by 안병길 on 2018-01-24.
 * Whya5448@gmail.com
 */

object AppVariables {

    val dateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
    val today: String = dateTime.format(DateTimeFormatter.ofPattern("MMdd"))
    val todayWithYear: String = dateTime.format(DateTimeFormatter.ofPattern("yyMMdd"))

    var baseDir:Path = Utils.getESODir().resolve("EsoKR")
    var poDir:Path = baseDir.resolve("PO_$today")
    var zanataCatDir:Path = baseDir.resolve("ZanataCategory")


}
