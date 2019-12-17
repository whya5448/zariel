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
    val yesterday: String = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(1).format(DateTimeFormatter.ofPattern("MMdd"))
    val todayWithYear: String = dateTime.format(DateTimeFormatter.ofPattern("yyMMdd"))

    const val WORK_DIR_PREFIX = "work_"
    const val ADDON_DIR = "addons"

    var baseDir: Path = Utils.getESODir().resolve("EsoKR")
    val addonDir: Path
        get() = baseDir.resolve(ADDON_DIR)
    val workDir: Path
        get() = baseDir.resolve("$WORK_DIR_PREFIX$today")
    val workAddonDir: Path
        get() = workDir.resolve(ADDON_DIR)
    val poDir: Path
        get() = workDir.resolve("po")

    val dirs: Array<Path>
        get() = arrayOf(poDir, workDir, addonDir, baseDir)
}
