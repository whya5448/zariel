package org.metalscraps.eso.lang.lib

import org.metalscraps.eso.lang.lib.bean.PO
import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*

/**
 * Created by 안병길 on 2019-02-06.
 * Whya5448@gmail.com
 */
class AddonManager {

    private val logger = LoggerFactory.getLogger(AddonManager::class.java)

    fun destination() {
        val vars = AppVariables

        class Quests(var id:String, var quest:String)
        class Runner(var en: Path, var ko: Path, var isEqualOrContains: Boolean = false,
                     var fileName: String, var key: String, var id: Int = 0) {

            fun process() {
                try {

                    // 영문 소스 불러옴
                    if(Files.notExists(en)) {
                        Files.createDirectories(en.parent)
                        Files.newBufferedWriter(en, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use {
                            it.write(URL("https://raw.githubusercontent.com/Whya5448/EsoKR/master/Destinations/data/EN/${en.fileName}").readText())
                        }
                    }
                    val enText = StringBuilder(Files.readString(vars.baseDir.resolve((en))))

                    // 한글 소스맵 불러옴
                    val koText = mutableMapOf<String, PO>()
                    Utils.listFiles(vars.poDir, "po2")
                            .stream()
                            .filter { e -> if (isEqualOrContains) e.fileName.toString() == fileName else e.fileName.toString().contains(fileName) }
                            .forEach { e -> koText.putAll(Utils.textParse(e, 2)) }

                    // 해당 Index 아닌 경우 불러온 데이터 삭제
                    koText.values.removeIf { x -> x.id1 != id }

                    // 영문 소스 객체-맵화
                    val enQuests = ArrayList<Quests>()
                    val questsMatcher = AppConfig.PATTERN_DESTINATION.matcher(enText)
                    while (questsMatcher.find()) enQuests.add(Quests(questsMatcher.group(2), questsMatcher.group(4)))

                    // 최종본 생성용 빌더
                    val builder = StringBuilder( "$key\n")

                    // 영문 맵에 있는 객체 ID로 한글맵에서 데이터 가져와 빌더에 붙힘.
                    for (x in enQuests) {
                        val xid = "$id-0-${x.id}"
                        val t = koText[xid]

                        if(t == null || t.isFuzzy) {
                            builder.append("\t[${x.id}] = {\"${x.quest}\"},\n")
                            if(t == null) logger.warn("Missing Data? $xid")
                            continue
                        }

                        builder.append("\t[${x.id}] = {\"${t.target}\"},\n")
                    }
                    builder.append("}\n")

                    // 불러왔던 원본 소스 replace
                    enText.replace(enText.indexOf(key), enText.length, builder.toString())

                    // 한글 결과물 있을 시 삭제 후 재작성
                    Files.createDirectories(ko.parent)
                    Files.deleteIfExists(ko)
                    Files.writeString(ko, enText, AppConfig.CHARSET)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }


        vars.run {
            var runner = Runner(
                    addonDir.resolve("Destinations/DestinationsQuests_en.lua"),
                    workAddonDir.resolve("Destinations/DestinationsQuests_kr.lua"),
                    true,
                    "journey.po2",
                    "QuestTableStore = {",
                    52420949
            )
            runner.process()

            runner = Runner(
                    addonDir.resolve("Destinations/DestinationsQuestgivers_en.lua"),
                    workAddonDir.resolve("Destinations/DestinationsQuestgivers_kr.lua"),
                    false,
                    "npc-talk",
                    "QuestGiverStore = {",
                    8290981
            )
            runner.process()

        }

    }
}
