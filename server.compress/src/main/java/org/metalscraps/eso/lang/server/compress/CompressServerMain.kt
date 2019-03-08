package org.metalscraps.eso.lang.server.compress

import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.config.ESOMain
import org.metalscraps.eso.lang.lib.util.Utils
import org.metalscraps.eso.lang.server.compress.config.CompressServerConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class CompressServerMain(var config:CompressServerConfig) : ESOMain {
    private val vars = AppVariables
    private val logger = LoggerFactory.getLogger(CompressServerMain::class.java)

    private fun deleteTemp() {
        try {
            Files.walk(vars.baseDir).filter { t -> !t.startsWith("${vars.baseDir}/PO_${vars.today}") && t.startsWith("${vars.baseDir}/PO_") }.forEach(System.out::println)
        } catch (e: IOException) {
            logger.error(e.message + " 이전 파일 삭제 실패")
            e.printStackTrace()
        }
    }

    private fun makeCSV() {
        vars.run {
            val listFiles = Utils.listFiles(poDir, "po2")
            val list = Utils.getMergedPO(listFiles)

            Utils.makeCSVwithLog(poDir.resolve("kr.csv"), list)
            Utils.makeCSVwithLog(poDir.resolve("kr_beta.csv"), list, beta = true)
            Utils.makeCSVwithLog(poDir.resolve("tr.csv"), list, writeSource = true)
            list.removeAll(list)
        }
    }

    override fun start() {
        vars.baseDir = config.workDir
        vars.poDir = config.workDir.resolve("PO_${vars.today}")

        logger.info(vars.dateTime.format(DateTimeFormatter.ofPattern("yy-MM-dd hh:mm:ss")) + " / 작업 시작")

        try {
            logger.info("잔여 파일 삭제")
            deleteTemp()
            
            vars.run {

                val scp = "scp -P ${config.mainServerPort}"
                val mainServerCredential = "${config.mainServerUserID}@${config.mainServerHostName}:"
                val lang = poDir.resolve("lang_$todayWithYear.7z")
                val dest = poDir.resolve("destinations_$todayWithYear.7z")
                val bucket = "gs://eso-team-waldo-bucket"

                logger.info("대상 다운로드")
                if(Files.notExists(poDir)) Files.createDirectories(poDir)
                Utils.processRun(poDir, "$scp $mainServerCredential${config.mainServerPOPath}$today/*.lua $poDir $mainServerCredential${config.mainServerPOPath}$today/po2.7z $poDir")
                Utils.processRun(poDir, "7za x po2.7z -y")

                logger.info("CSV 생성")
                makeCSV()

                logger.info("대상 압축")
                Utils.processRun(poDir, "7za a -mx=7 $lang $poDir/*.csv")
                Utils.processRun(poDir, "7za a -mx=7 $dest $poDir/*.lua")

                logger.info("SFX 생성")
                Utils.processRun(poDir, "cat $baseDir/7zCon.sfx $lang", ProcessBuilder.Redirect.to(Paths.get("$lang.exe").toFile()))
                Utils.processRun(poDir, "cat $baseDir/7zCon.sfx $dest", ProcessBuilder.Redirect.to(Paths.get("$dest.exe").toFile()))

                logger.info("기존 업로드된 목적파일 삭제")
                Utils.processRun(poDir, "gsutil rm $bucket/lang*.exe")
                Utils.processRun(poDir, "gsutil rm $bucket/dest*.exe")

                logger.info("목적파일 업로드")
                Utils.processRun(poDir, "gsutil cp $lang.exe $bucket/")
                Utils.processRun(poDir, "gsutil cp $dest.exe $bucket/")

                logger.info("버전 문서 생성")
                Utils.processRun(poDir, "echo ${Date().time}/$todayWithYear/${Utils.crc32(Paths.get("$lang.exe"))}", ProcessBuilder.Redirect.to(poDir.resolve("ver.html").toFile()))

                logger.info("버전 문서 업로드")
                Utils.processRun(poDir, "$scp ${poDir.resolve("ver.html")} $mainServerCredential${config.mainServerVersionDocumentPath}")
            }

            System.exit(0)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

}
