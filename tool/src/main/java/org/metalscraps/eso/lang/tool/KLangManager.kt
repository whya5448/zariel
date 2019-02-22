package org.metalscraps.eso.lang.tool

import org.metalscraps.eso.lang.lib.bean.ToCSVConfig
import org.metalscraps.eso.lang.lib.bean.XPO
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.util.KUtils
import org.metalscraps.eso.lang.lib.util.KUtils.Companion.getMergedPO
import org.metalscraps.eso.lang.lib.util.KUtils.Companion.listFiles
import org.metalscraps.eso.lang.lib.util.KUtils.Companion.makeCSV
import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Component
class KLangManager {

    companion object { val logger: Logger = LoggerFactory.getLogger(KLangManager::class.java) }
    private var vars:AppVariables = AppVariables

    fun makeCSVwithLog(path: Path, list: MutableList<XPO>, writeSource:Boolean = false, writeFileName:Boolean = false, beta:Boolean = false) {
        val timeTaken = LocalTime.now()
        makeCSV(path, list, writeSource, writeFileName, beta)
        logger.info("${path.fileName} ${timeTaken.until(LocalTime.now(), ChronoUnit.SECONDS)}초")
    } // makeCSVwithLog

    internal fun makeCSVs() {
        val listFiles = listFiles(vars.poDir, "po2")

        // 합쳐서 csv 로 한번에 생성
        val list = getMergedPO(listFiles)

        makeCSVwithLog(vars.poDir.resolve("kr.csv"), list)
        makeCSVwithLog(vars.poDir.resolve("kr_beta.csv"), list, beta = true)
        makeCSVwithLog(vars.poDir.resolve("tr.csv"), list, writeFileName = true)

    } // makeCSVs

    internal fun something() {
        val data = KUtils.getMergedPO(KUtils.listFiles(vars.poDir, "po"))
        val sb = StringBuilder()
        data.forEach { e:XPO -> sb.append(e.toCSVFormat()) }
        logger.info(vars.baseDir.toString())
        Files.writeString(vars.baseDir.resolve("kr.csv"), sb.toString())
    } // something

    internal fun lineCompare() {
        val en = KUtils.textParse(vars.baseDir.resolve("en.lang.csv"))
        logger.info("en.lang.csv ${en.size}행")
        val ko = KUtils.textParse(vars.baseDir.resolve("kr.csv"))
        logger.info("kr.csv ${ko.size}행")
        ko.keys.forEach { x -> en.remove(x) }
        en.values.forEach { x -> logger.info("$x") }
        logger.info("${en.size}행 모자람.")
    } // lineCompare

}
