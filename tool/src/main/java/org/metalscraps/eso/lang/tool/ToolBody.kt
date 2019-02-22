package org.metalscraps.eso.lang.tool

import org.jsoup.helper.StringUtil
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.util.KUtils.Companion.convertKO_POtoCN
import org.metalscraps.eso.lang.lib.util.KUtils.Companion.downloadPOs
import org.metalscraps.eso.lang.lib.util.Utils.convertKO_PO_to_CN
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

@Component
class ToolBody(val manager: KLangManager) {

    private var vars:AppVariables = AppVariables

    companion object { private val logger = LoggerFactory.getLogger(ToolApplication::class.java) }
    private fun showMessage() {
        logger.info("baseDir : " + vars.baseDir)
        logger.info("PODir : " + vars.poDir)
        logger.info("0. CSV To PO")
        logger.info("1. Zanata PO 다운로드")
        logger.info("2. PO 폰트 매핑/변환")
        logger.info("3. CSV 생성")
        logger.info("4. 기존 번역물 합치기")
        logger.info("44. 기존 번역물 합치기 => JSON")
        logger.info("5. 다!")

        logger.info("9. 종료")
        logger.info("11. TTC")
        logger.info("12. Destinations")
        logger.info("100. PO -> 구글 번역 (beta)")
        logger.info("300. Zanata upload용 csv category 생성")
    }

    fun start(args:Array<String>) {
        var command = ""

        try { Files.createDirectories(vars.baseDir) }
        catch (e: IOException) {
            logger.error("작업 폴더 생성 실패" + e.message)
            e.printStackTrace()
            System.exit(-1)
        }

        logger.info(StringUtil.join(args, " "))
        for (x in args) {
            when {
                x.startsWith("-opt") -> command = x.substring(x.indexOf('=') + 1)
                x.startsWith("-base") -> vars.baseDir = Paths.get(x.substring(x.indexOf('=') + 1))
                x.startsWith("-po") -> vars.poDir = Paths.get(x.substring(x.indexOf('=') + 1))
            }
        }

        //val CG = CategoryGenerator(vars)
        if (!Files.exists(vars.poDir)) try { Files.createDirectories(vars.poDir) } catch (ignored: IOException) { }
        when (command) {
            "help" -> showMessage()
            /*
            "0" -> lm.CsvToPo()
            "4" -> lm.makeLang()
            "34" -> {
                lm.makeCSVs()
                lm.makeLang()
            }
            "44" -> lm.makeLangToJSON()
            "5" -> {
                Utils.downloadPOs(vars)
                Utils.convertKO_PO_to_CN(vars)
                lm.makeCSVs()
                lm.makeLang()
                AddonManager(vars).destination()
            }
            "6" -> AddonManager(vars).destination()
            "7" -> lm.something()
            "9" -> {
                System.exit(0)
                TamrielTradeCentre(vars).start()
            }
            "11" -> TamrielTradeCentre(vars).start()
            "100" -> lm.translateGoogle()
            "200" -> CG.GenCategory()
            "300" -> lm.GenZanataUploadSet()
            */
            "1" -> downloadPOs()
            "2" -> convertKO_POtoCN()
            "3" -> manager.makeCSVs()
            "66" -> manager.lineCompare()
            "7" -> manager.something()
            else -> logger.error("command not found. $command")
        }


    }

}