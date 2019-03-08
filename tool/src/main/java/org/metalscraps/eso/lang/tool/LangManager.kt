package org.metalscraps.eso.lang.tool

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.metalscraps.eso.lang.lib.bean.PO
import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.util.Utils
import org.metalscraps.eso.lang.lib.util.Utils.Companion.getMergedPO
import org.metalscraps.eso.lang.lib.util.Utils.Companion.listFiles
import org.metalscraps.eso.lang.lib.util.Utils.Companion.makeCSV
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class LangManager(private val objectMapper: ObjectMapper) {

    companion object { val logger: Logger = LoggerFactory.getLogger(LangManager::class.java) }
    private var vars:AppVariables = AppVariables

    fun makeCSVwithLog(path: Path, list: MutableList<PO>, writeSource:Boolean = false, writeFileName:Boolean = false, beta:Boolean = false) {
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

    internal fun makeLang() {

        // EsoExtractData.exe -l en_0124.lang -p
        try {
            Utils.processRun(vars.baseDir, "${vars.baseDir}/EsoExtractData v0.32/EsoExtractData.exe -p -x kr.csv -o kr.lang")
            Utils.processRun(vars.baseDir, "${vars.baseDir}/EsoExtractData v0.32/EsoExtractData.exe -p -x kr_beta.csv -o kr_beta.lang")
            Utils.processRun(vars.baseDir, "${vars.baseDir}/EsoExtractData v0.32/EsoExtractData.exe -p -x tr.csv -o tr.lang")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    } // makeLang

    internal fun something() {
        val data = Utils.getMergedPO(Utils.listFiles(vars.poDir, "po"))
        val sb = StringBuilder()
        data.forEach { e:PO -> sb.append(e.toCSVFormat()) }
        logger.info(vars.baseDir.toString())
        Files.writeString(vars.baseDir.resolve("kr.csv"), sb.toString())
    } // something

    internal fun lineCompare() {
        val en = Utils.textParse(vars.baseDir.resolve("en.lang.csv"))
        logger.info("en.lang.csv ${en.size}행")
        val ko = Utils.textParse(vars.baseDir.resolve("kr.csv"))
        logger.info("kr.csv ${ko.size}행")
        ko.keys.forEach { x -> en.remove(x) }
        en.values.forEach { x -> logger.info("$x") }
        logger.info("${en.size}행 모자람.")
    } // lineCompare

    fun enCSVtoPOT() {
        val mapper = objectMapper
        val cat:HashMap<String, Array<Int>> = mapper.readValue(Files.readString(vars.baseDir.resolve("IndexMatch_modified.json"), AppConfig.CHARSET))
        val list = Utils.textParse(vars.baseDir.resolve("en.lang.csv")).values.toMutableList()
        Collections.sort(list, PO.comparator)

        cat.forEach { k, vl ->
            var sb = getStringBuilderForPOT()
            vl.forEach { list.filter { po -> po.id1 == it }.forEach { po -> list.remove(po); sb.append(po.toPOTFormat()) } }
            sb = escapeStringForPOT(sb)
            Files.writeString(vars.baseDir.resolve("./pot/$k.pot"), sb, AppConfig.CHARSET)
        }

        var sb = getStringBuilderForPOT()
        list.forEach { sb.append(it.toPOTFormat())  }
        sb = escapeStringForPOT(sb)
        Files.writeString(vars.baseDir.resolve("./pot/etc.pot"), sb, AppConfig.CHARSET)
    }

    private fun getStringBuilderForPOT(): StringBuilder {
        return StringBuilder("""
            #, fuzzy
            msgid ""
            msgstr ""
            "MIME-Version: 1.0\n"
            "Content-Transfer-Encoding: 8bit\n"
            "Content-Type: text/plain; charset=UTF-8\n"
        """.trimIndent())
    }

    private fun escapeStringForPOT(sb: StringBuilder): StringBuilder {
        // EsoUI\Art\TreeIcons\achievements_indexIcon_collections_up.dds => EsoUI\\Art\\TreeIcons\\achieveme~
        // tip.pot-62156964-0-290
        // "\\"
        // tip.pot-41714900-0-307 기타 등등 \ 이스케이프 문자 \n 제외하고 전부 \\로 이중 이스케이프
        var vSB = StringBuilder(sb.replace("\\\\(?!n)".toRegex(), "\\\\\\\\"))
        // "Lorem Ipsum is\nsimply" => "Lorem Ipsum is" + \n + "\\\\\nsimply" -> \n 자나타에서 \n 파싱 안됨
        vSB = StringBuilder(vSB.replace("\\\\n".toRegex(), "\"\n\"\\\\n"))
        return vSB
    }

    fun updateCategory() {
        //val mapper = objectMapper
        //val cat:HashMap<String, Array<Int>> = mapper.readValue(Files.readString(vars.baseDir.resolve("categories.json"), AppConfig.CHARSET))
        Utils.getDefaultRestClient("https://esoitem.uesp.net/viewMinedItems.php")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
