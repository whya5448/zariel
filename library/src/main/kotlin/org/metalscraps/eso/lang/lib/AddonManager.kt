package org.metalscraps.eso.lang.lib

import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.util.Utils
import org.metalscraps.eso.lang.lib.util.fileName
import org.slf4j.LoggerFactory
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.HashMap
import java.util.regex.Pattern


/**
 * Created by 안병길 on 2019-02-06.
 * Whya5448@gmail.com
 */
class AddonManager {

    private val logger = LoggerFactory.getLogger(AddonManager::class.java)
    private val vars = AppVariables

    companion object {
        private const val CDN = "https://rawcdn.githack.com/Whya5448/EsoKR"
    }

    fun getSource(sPath: String, commit: String): StringBuilder {
        val addonPath = vars.addonDir.resolve(sPath)
        if (Files.notExists(addonPath) || (Files.exists(addonPath) && (Files.size(addonPath) <= 0L))) {
            Files.createDirectories(addonPath.parent)
            Files.newBufferedWriter(addonPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).use {
                it.write(URL("$CDN/$commit/$sPath").readText())
            }
        }
        return StringBuilder(Files.readString(addonPath, AppConfig.CHARSET))
    }

    fun destination(lang: String = "kr", writeFileName: Boolean = false, beta: Boolean = false) {
        val commit = "5450c42d1a037ff907906c8361b077a398983c0d"

        class Runner(var en: String, var ko: Path, var isEqualOrContains: Boolean = false,
                     var fileName: String, var key: String, var id: Int = 0) {

            fun process() {
                try {
                    val enText = getSource(en, commit)

                    // 한글 소스맵 불러옴
                    val list = Utils.listFiles(vars.poDir, "po").filter { if (isEqualOrContains) it.fileName.toString() == fileName else it.fileName.toString().contains(fileName) }
                    val koText = Utils.getMergedPOtoMap(list, Utils.TextParseOptions(toChineseOffset = false))
                    val missingList = ArrayList<String>()

                    // 해당 Index 아닌 경우 불러온 데이터 삭제
                    koText.values.removeIf { x -> x.id1 != id }

                    // 영문 소스 객체-맵화
                    val enQuests = ArrayList<Pair<String, String>>()
                    val questsMatcher = AppConfig.PATTERN_DESTINATION.matcher(enText)
                    while (questsMatcher.find()) enQuests.add(Pair(questsMatcher.group(2), questsMatcher.group(4)))

                    // 최종본 생성용 빌더
                    val builder = StringBuilder("$key\n")

                    // 영문 맵에 있는 객체 ID로 한글맵에서 데이터 가져와 빌더에 붙힘.
                    for (x in enQuests) {
                        val xid = "$id-0-${x.first}"
                        val t = koText[xid]

                        // 한글 맵에 없을경우엔 그냥 무시 → 인게임 애드온에서 계속 Missing NPC 뜸
                        if (t == null) {
                            missingList.add("$xid $x")
                            builder.append("\t[${x.first}] = {\"${x.second}\"},\n")
                        } else builder.append("\t[${x.first}] = {EsoKR:E(\"${t.getText(writeFileName, beta)}\")},\n")

                    }
                    builder.append("}\n")
                    if(missingList.size > 0) logger.warn("missingData $missingList")

                    // 불러왔던 원본 소스 replace
                    enText.replace(enText.indexOf(key), enText.length, builder.toString())

                    // 한글 결과물 있을 시 삭제 후 재작성
                    Files.createDirectories(ko.parent)
                    Files.deleteIfExists(ko)
                    Files.writeString(ko, enText, AppConfig.CHARSET)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }


        vars.run {
            var runner = Runner(
                    "Destinations/data/EN/DestinationsQuests_en.lua",
                    workAddonDir.resolve("Destinations/DestinationsQuests_$lang.lua"),
                    false,
                    "journey.po",
                    "QuestTableStore = {",
                    52420949
            )
            runner.process()

            runner = Runner(
                    "Destinations/data/EN/DestinationsQuestgivers_en.lua",
                    workAddonDir.resolve("Destinations/DestinationsQuestgivers_$lang.lua"),
                    false,
                    "npc-talk",
                    "QuestGiverStore = {",
                    8290981
            )
            runner.process()

        }

    }

    fun tamrielTradeCentre(lang: String = "kr", writeFileName: Boolean = false, beta: Boolean = false) {
        val commit = "5450c42d1a037ff907906c8361b077a398983c0d"

        // ESO-item
        val pattern = arrayOf(
                "^Trophy","^Treasure","^Trash","^Tool","^Tabard","^Soul Gem","^Furni","^None","^Plug","^Siege","^Ava Repair","^Container","^Costume",
                "^Crown Store","^Disguise","^Drink","^Dye Stamp","^Fish","^Food",
                "^item","^Weapon","^Armor","^Clothier","^Blacksmith","^Woodwork","^Jewelry","^Poison","^Potion",
                "^Raw Mat","^Style Mat","^Reagent","^Ingredi","^Recipe","^Master Writ","^Motif","^Collectible","^Lure"," Rune$"
        ).map { it.toRegex(RegexOption.IGNORE_CASE) }

        class Runner(var en: String, var ko: Path) {
            fun process() {

                val enMap = HashMap<String, Pair<String, String>>()
                val enText = getSource(en, commit)
                enText.replace(enText.length-6, enText.length, "")

                val fileList = Utils.listFiles().filter { p-> pattern.any { p.fileName().contains(it) } } as MutableList
                val koMap = Utils.getMergedPOtoMap(fileList,
                        Utils.TextParseOptions(
                                toLower = true,
                                keyGroup = 6,
                                toChineseOffset = false,
                                parseSource = true,
                                excludeId = arrayOf(
                                        "40741187", // item-type
                                        "263796174",
                                        "132143172",
                                        "249673710",
                                        "228378404",
                                        "211640654",
                                        "681322639",
                                        "132143172"
                                ).map { "^$it-".toRegex() }.toTypedArray()
                        ))

                val m = Pattern.compile("(\\[\"([\\w\\d\\s,:'()-]+)\"]=\\{\\[(\\d+)]=(\\w+),},)", Pattern.MULTILINE).matcher(enText)
                while (m.find()) enMap[m.group(2)] = Pair(m.group(3), m.group(4))

                val key = ",},"
                val value = "$key\n"
                var idx = enText.indexOf(key, 0)
                while (idx != -1) {
                    enText.replace(idx, idx+3, value)
                    idx = enText.indexOf(key, idx+4)
                }

                for (entry in koMap.entries)
                    enMap[entry.key]?.let {
                        val v = entry.value.getText(writeFileName, beta);
                        if(entry.key != v) enText.append("[EsoKR:E(\"$v\")]={[${it.first}]=${it.second},},\n")
                    }
                enText.delete(enText.length-2, enText.length)
                enText.append("}\nend")
                //eText.replace(0, 71, "function TamrielTradeCentre:LoadItemLookUpTable()\nself.ItemLookUpTable")

                Files.createDirectories(ko.parent)
                Files.deleteIfExists(ko)
                Files.writeString(ko, enText, AppConfig.CHARSET)
            }
        }

        vars.run {
            val runner = Runner(
                    "TamrielTradeCentre/ItemLookUpTable_EN.lua",
                    workAddonDir.resolve("TamrielTradeCentre/ItemLookUpTable_$lang.lua")
            )
            runner.process()
        }

    }
}

