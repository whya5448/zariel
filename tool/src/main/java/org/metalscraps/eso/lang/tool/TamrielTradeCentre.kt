package org.metalscraps.eso.lang.tool

import org.metalscraps.eso.lang.lib.bean.PO
import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.config.AppWorkConfig
import org.metalscraps.eso.lang.lib.config.FileNames
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig
import org.metalscraps.eso.lang.lib.util.Utils
import java.nio.file.Files
import java.util.*
import java.util.regex.Pattern

/**
 * Created by 안병길 on 2017-12-17.
 * Whya5448@gmail.com
 */

internal class TamrielTradeCentre(private val config: AppWorkConfig) {

    internal inner class LuaClass(val x: String, val y: String) {

        override fun equals(other: Any?): Boolean {
            if (other === this) return true
            if (other !is LuaClass || this.x != other.x) return false
            return this.y == other.y
        }

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y.hashCode()
            return result
        }
    }

    fun start() {

        try {

            val fileNames = arrayOf(FileNames.item, FileNames.itemOther)

            val koreanTable = config.baseDirectoryToPath.resolve("TTC/ItemLookUpTable_KR.lua")
            val englishTable = config.baseDirectoryToPath.resolve("TTC/ItemLookUpTable_EN.lua")

            val poHashMap = HashMap<String, PO>()
            val poHashMapWithTitle: HashMap<String, PO>

            val sourceToMapConfig = SourceToMapConfig()
                    .setKeyGroup(6)
                    .setToLowerCase(true)
                    .setPattern(AppConfig.POPattern)

            // 번역본에서 데이터 추출
            for (fileName in fileNames) {
                sourceToMapConfig.path = config.poDirectoryToPath.resolve(fileName.toStringPO2())
                poHashMap.putAll(Utils.sourceToMap(sourceToMapConfig))
            }

            // 타이틀 버전
            poHashMapWithTitle = HashMap(poHashMap)
            for (p in poHashMapWithTitle.values) p.target = p.fileName.shortName + "_" + p.id3 + "_" + p.target

            // 룩업 테이블 정보화
            val englishSource = StringBuilder(Files.readString(englishTable, AppConfig.CHARSET).toLowerCase().replace("},}\\s*end\\s*".toRegex(), "},"))

            val englishMap = HashMap<String, LuaClass>()
            val p = Pattern.compile("(\\[\"([\\w\\d\\s,:'()-]+)\"]=\\{\\[(\\d+)]=(\\w+),},)", Pattern.MULTILINE)
            val m = p.matcher(englishSource)
            while (m.find()) englishMap[m.group(2)] = LuaClass(m.group(3), m.group(4))

            val sb = StringBuilder()
            for ((key, value) in poHashMap) {
                val d = englishMap[key]
                if (d != null)
                    sb.append("[\"")
                            .append(value.target)
                            .append("\"]={[")
                            .append(d.x)
                            .append("]=")
                            .append(d.y)
                            .append(",},")
            }

            for ((key, value) in poHashMapWithTitle) {
                val d = englishMap[key]
                if (d != null)
                    sb.append("[\"")
                            .append(value.target)
                            .append("\"]={[")
                            .append(d.x)
                            .append("]=")
                            .append(d.y)
                            .append(",},")
            }

            englishSource.append(Utils.KOToCN(sb.toString())).append("}\nend")

            val key = ",},"
            val value = ",},\n"
            var start = englishSource.indexOf(key, 0)
            while (start > -1) {
                val end = start + key.length
                val nextSearchStart = start + value.length
                englishSource.replace(start, end, value)
                start = englishSource.indexOf(key, nextSearchStart)
            }

            englishSource.replace(0, 71, "function TamrielTradeCentre:LoadItemLookUpTable()\nself.ItemLookUpTable")

            Files.writeString(koreanTable, englishSource.toString(), AppConfig.CHARSET)
            Files.copy(koreanTable, koreanTable.resolveSibling(koreanTable.fileName.toString().replace("_KR","_TR")))

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
