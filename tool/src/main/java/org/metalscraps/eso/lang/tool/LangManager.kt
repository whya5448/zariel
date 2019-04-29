package org.metalscraps.eso.lang.tool

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.metalscraps.eso.lang.lib.bean.PO
import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.util.Utils
import org.metalscraps.eso.lang.tool.vo.Category
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.nio.file.Files
import java.util.*
import java.util.regex.Pattern

@Component
class LangManager(private val objectMapper: ObjectMapper) {

    companion object { val logger: Logger = LoggerFactory.getLogger(LangManager::class.java) }
    private var vars:AppVariables = AppVariables

    internal fun makeLang() {
        vars.run {
            val list = Utils.getMergedPO(Utils.listFiles(poDir, "po"))
            Utils.makeLANGwithLog(workDir.resolve("kr.lang"), list)
            Utils.makeLANGwithLog(workDir.resolve("kb.lang"), list, writeFileName = true)
            Utils.makeLANGwithLog(workDir.resolve("tr.lang"), list, beta = true)
        }

    } // makeLang

    internal fun something() {
        val data = Utils.getMergedPO(Utils.listFiles(vars.poDir, "po"))
        val sb = StringBuilder()
        data.forEach { e:PO -> sb.append(e.toPOTFormat()) }
        logger.info(vars.baseDir.toString())
        Files.writeString(vars.baseDir.resolve("kr.pot"), sb.toString())
    } // something

    internal fun lineCompare() {
        val en = Utils.textParse(vars.baseDir.resolve("en.lang.csv"))
        logger.info("en.lang.csv ${en.size}행")
        val ko = Utils.textParse(vars.baseDir.resolve("kr.csv"))
        logger.info("kr.csv ${ko.size}행")
        ko.keys.forEach { x -> en.remove(x) }
        en.values.forEach { x -> logger.info("$x") }
        logger.info("${en.size}행 모자람.")
        val sb = StringBuilder()
        en.values.forEach { sb.append("$it\n") }
        Files.writeString(vars.baseDir.resolve("compare.txt"), sb.toString(), AppConfig.CHARSET)
    } // lineCompare

    fun enCSVtoPOT() {
        val list = Utils.textParse(vars.baseDir.resolve("en.lang.csv")).values.toMutableList()
        Collections.sort(list, PO.comparator)

/*
        val mapper = objectMapper
        val cat:HashMap<String, Array<Int>> = mapper.readValue(Files.readString(vars.baseDir.resolve("IndexMatch_modified.json"), AppConfig.CHARSET))
        cat.forEach { k, vl ->
            var sb = StringBuilder()
            vl.forEach { list.filter { po -> po.id1 == it }.forEach { po -> list.remove(po); sb.append(po.toPOTFormat()) } }
            sb = escapeStringForPOT(sb)
            sb.insert(0, getPOTTemplate())
            Files.writeString(vars.baseDir.resolve("./pot/$k.pot"), sb, AppConfig.CHARSET)
        }
*/

        var sb = StringBuilder()
        list.forEach { sb.append(it.toPOTFormat())  }
        sb = escapeStringForPOT(sb)
        sb.insert(0, getPOTTemplate())
        Files.writeString(vars.baseDir.resolve("./pot/kr.pot"), sb, AppConfig.CHARSET)
    }

    private fun getPOTTemplate(): String {
        return """
            #, fuzzy
            msgid ""
            msgstr ""
            "MIME-Version: 1.0\n"
            "Content-Transfer-Encoding: 8bit\n"
            "Content-Type: text/plain; charset=UTF-8\n"


        """.trimIndent()
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
        val mapper = objectMapper
        val cat = mapper.readTree(Files.readString(vars.baseDir.resolve("categories.json"), AppConfig.CHARSET))
        val array = JsonNodeFactory.instance.objectNode()
        val domain = "https://esoitem.uesp.net/viewMinedItems.php"
        array.put("URL", domain)

        val response: HttpResponse<String> = HttpClient.newHttpClient().send(Utils.getDefaultRestClient(domain), HttpResponse.BodyHandlers.ofString())
        var body = response.body()
        body = body.substring(body.indexOf("esovmi_list'>")+"esovmi_list'>".length, body.indexOf("</ol>"))
        val m = Pattern.compile("<a href='\\?type=(\\d+?)'>(.+?) \\((\\d+) items?\\)</a>").matcher(body)
        while(m.find()) {
            val id = m.group(1).toInt()
            val category = Category(m.group(1).toInt(), m.group(2), volume = m.group(3).toInt())
            cat["group"].fields().forEach { if(it.value.map { x -> x.asInt() }.contains(id)) category.category = it.key }
            if(category.category == category.resourceName) array.putPOJO(id.toString(), category)
        }
        (cat["categories"] as ObjectNode).set("item", array)

        Files.writeString(vars.baseDir.resolve("categories.json"), mapper.writer().withDefaultPrettyPrinter().writeValueAsString(cat))

    }
}
