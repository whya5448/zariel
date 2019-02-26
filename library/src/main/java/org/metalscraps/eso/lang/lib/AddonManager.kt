package org.metalscraps.eso.lang.lib

import org.metalscraps.eso.lang.lib.bean.PO
import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * Created by 안병길 on 2019-02-06.
 * Whya5448@gmail.com
 */
class AddonManager(var vars:AppVariables) {

    private val logger = LoggerFactory.getLogger(AddonManager::class.java)

    fun destination() {

        class Quests(var id:String, var quest:String)
        class Runner(var en: Path, var ko: Path, var nameCheck: Boolean = false,
                     var fileName: String, var key: String, var id: Int = 0) {

            fun process() {
                try {

                    // 영문 소스 불러옴
                    val enText = StringBuilder(Files.readString(vars.baseDir.resolve(en)))

                    // 한글 소스맵 불러옴
                    val koText = mutableMapOf<String, PO>()
                    Utils.listFiles(vars.poDir, "po2")
                            .stream()
                            .filter { e -> if (nameCheck) e.fileName.toString() == fileName else e.fileName.toString().contains(fileName) }
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
                        if (!koText[xid]!!.isFuzzy) builder.append("\t[${x.id}] = {\"${koText[xid]!!.target}\"},\n")
                        else {
                            builder.append("\t[${x.id}] = {\"${x.quest}\"},\n")
                            if (koText[xid] == null) logger.warn("Missing Data? $xid")
                        }
                    }
                    builder.append("}\n")

                    // 불러왔던 원본 소스 replace
                    enText.replace(enText.indexOf(key), enText.length, builder.toString())

                    // 한글 결과물 있을 시 삭제 후 재작성
                    Files.deleteIfExists(ko)
                    Files.writeString(ko, enText, AppConfig.CHARSET)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }


        var runner = Runner(
                vars.baseDir.resolve("DestinationsQuests_en.lua"),
                vars.baseDir.resolve("DestinationsQuests_kr.lua"),
                true,
                "journey.po2",
                "QuestTableStore = {",
                52420949
        )
        runner.process()

        runner = Runner(
                vars.baseDir.resolve("DestinationsQuestgivers_en.lua"),
                vars.baseDir.resolve("DestinationsQuestgivers_kr.lua"),
                false,
                "npc-talk",
                "QuestGiverStore = {",
                8290981
        )
        runner.process()

    }
}
