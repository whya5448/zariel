package org.metalscraps.eso.lang.tool

import org.metalscraps.eso.lang.lib.AddonManager
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.util.Utils.Companion.convertKO_POtoCN
import org.metalscraps.eso.lang.lib.util.Utils.Companion.downloadPOs
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

@Component
class ToolMain(val mgr: LangManager) {

    private var vars:AppVariables = AppVariables

    companion object { private val logger = LoggerFactory.getLogger(ToolApplication::class.java) }

    fun start(args:Array<String>) {
        var command = ""

        try { Files.createDirectories(vars.baseDir) }
        catch (e: IOException) {
            logger.error("작업 폴더 생성 실패" + e.message)
            e.printStackTrace()
            System.exit(-1)
        }


        logger.info(args.joinToString())
        for (x in args) {
            logger.debug("args $x")
            when {
                x.startsWith("-opt") -> command = x.substring(x.indexOf('=') + 1)
                x.startsWith("-base") -> vars.baseDir = Paths.get(x.substring(x.indexOf('=') + 1))
                x.startsWith("-po") -> vars.poDir = Paths.get(x.substring(x.indexOf('=') + 1))
            }
        }

        //val CG = CategoryGenerator(vars)
        if (!Files.exists(vars.poDir)) try { Files.createDirectories(vars.poDir) } catch (ignored: IOException) { }
        when (command) {
            /*
            "0" -> lm.CsvToPo()
            */
            "1" -> downloadPOs()
            "2" -> convertKO_POtoCN()
            "3" -> mgr.makeCSVs()
            "34" -> {
                mgr.makeCSVs()
                mgr.makeLang()
            }
            "4" -> mgr.makeLang()
            "5" -> {
                downloadPOs()
                convertKO_POtoCN()
                mgr.makeCSVs()
                mgr.makeLang()
                AddonManager(vars).destination()
            }
            "6" -> AddonManager(vars).destination()
            "66" -> mgr.lineCompare()
            "7" -> mgr.something()
            "8" -> mgr.enCSVtoPOT()
            "9" -> mgr.updateCategory()
            else -> logger.error("command not found. $command")
        }


    }

}