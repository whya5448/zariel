package org.metalscraps.eso.lang.lib.config

import org.metalscraps.eso.lang.lib.util.KUtils
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Created by 안병길 on 2018-01-24.
 * Whya5448@gmail.com
 */

object AppVariables {

    private val dateTime: LocalDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
    val today: String = dateTime.format(DateTimeFormatter.ofPattern("MMdd"))
    val todayWithYear: String = dateTime.format(DateTimeFormatter.ofPattern("yyMMdd"))

    var baseDir:Path = KUtils.getESODir().resolve("EsoKR")
    var poDir:Path = baseDir.resolve("PO_$today")
    var zanataCatDir:Path = baseDir.resolve("ZanataCategory")


}
