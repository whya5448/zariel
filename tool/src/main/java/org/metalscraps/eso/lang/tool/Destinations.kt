package org.metalscraps.eso.lang.tool

import org.metalscraps.eso.lang.lib.bean.PO
import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.config.AppWorkConfig
import org.metalscraps.eso.lang.lib.config.FileNames
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig
import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * Created by 안병길 on 2018-01-15.
 * Whya5448@gmail.com
 */


internal class Destinations(private val config: AppWorkConfig) {
    private val logger = LoggerFactory.getLogger(Destinations::class.java)

    private fun work(fileNames: Array<FileNames>, target: Path) {

        val poMap = HashMap<String, PO>()

        try {
            val destinationQuestSource = StringBuilder(Files.readString(target.resolveSibling(target.fileName.toString().replace("kr", "en"))))
            val config = SourceToMapConfig().setPattern(AppConfig.POPattern).setKeyGroup(6).setPrefix("{\"").setSuffix("\"}").setProcessItemName(false)

            for (fileName in fileNames) poMap.putAll(Utils.sourceToMap(config.setPath(this.config.poDirectoryToPath.resolve(fileName.toStringPO2()))))

            var init = false
            for ((key, po) in HashMap(poMap)) {
                if (!init) {
                    poMap.clear()
                    init = true
                }
                if (po.isFuzzy) po.target = po.source
                poMap["{\"$key\"}"] = po
            }

            // 찾아바꾸기
            Utils.replaceStringFromMap(destinationQuestSource, poMap)

            // 저장
            logger.info("" + target)
            Files.writeString(target, destinationQuestSource, AppConfig.CHARSET)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun start() {

        work(arrayOf(FileNames.journey, FileNames.journeyOther), config.baseDirectoryToPath.resolve("Destinations/DestinationsQuests_kr.lua"))
        work(arrayOf(FileNames.npcName), config.baseDirectoryToPath.resolve("Destinations/DestinationsQuestgivers_kr.lua"))

    }

}
