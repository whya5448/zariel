package org.metalscraps.eso.lang.server.compress

import org.metalscraps.eso.lang.lib.config.AppWorkConfig
import org.metalscraps.eso.lang.lib.config.ESOConfigOptions
import org.metalscraps.eso.lang.lib.util.Utils
import org.metalscraps.eso.lang.server.compress.config.CompressServerConfig
import org.metalscraps.eso.lang.server.compress.config.CompressServerConfig.CompressServerConfigOptions.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.collections.HashMap
import kotlin.collections.mapOf
import kotlin.collections.toMap

class CompressServerMain internal constructor() {
    private val appWorkConfig = AppWorkConfig()
    private val cConf = CompressServerConfig(Paths.get("."), Paths.get("./.config"))

    init {
        logger.info("설정 불러오기")
        cConf.load(mapOf(
                MAIN_SERVER_HOST_NAME to "hostname",
                MAIN_SERVER_PORT to "22",
                MAIN_SERVER_USER_ID to "username",
                MAIN_SERVER_PO_PATH to "/path/to/po",
                MAIN_SERVER_VERSION_DOCUMENT_PATH to "/path/to/doc",
                WORK_DIR to "."
        ).toMap(HashMap<ESOConfigOptions, Any>()))
    }

    private fun deleteTemp() {
        try {
            Files.walk(appWorkConfig.baseDirectoryToPath).filter { x -> Files.isRegularFile(x) && (x.toString().endsWith(".csv") ||
                    x.toString().endsWith(".7z") || x.toString().endsWith(".7z.exe") || x.toString().endsWith(".html")) }
                    .forEach { path -> try { Files.delete(path) } catch (e: IOException) { e.printStackTrace() } }
        } catch (e: IOException) {
            logger.error(e.message + " 이전 파일 삭제 실패")
            e.printStackTrace()
        }
    }

    private fun start() {

        logger.info(appWorkConfig.dateTime.format(DateTimeFormatter.ofPattern("yy-MM-dd hh:mm:ss")) + " / 작업 시작")
        appWorkConfig.baseDirectoryToPath = cConf.workDir
        appWorkConfig.poDirectoryToPath = appWorkConfig.baseDirectoryToPath.resolve("PO_" + appWorkConfig.today)

        try {
            logger.info("잔여 파일 삭제")
            deleteTemp()
            
            appWorkConfig.run {
                val scp = "scp -P ${cConf.mainServerPort}"
                val mainServerCredential = "${cConf.mainServerUserID}@${cConf.mainServerHostName}:"
                val lang = baseDirectoryToPath.resolve("lang_$todayWithYear.7z")
                val dest = baseDirectoryToPath.resolve("destinations_$todayWithYear.7z")
                val bucket = "gs://eso-team-waldo-bucket"

                logger.info("대상 다운로드")
                logger.info("$scp $mainServerCredential${cConf.mainServerPOPath}$today/../*.lua ${cConf.workDir} $mainServerCredential${cConf.mainServerPOPath}$today/*.csv ${cConf.workDir}")
                Utils.processRun(baseDirectoryToPath, "$scp $mainServerCredential${cConf.mainServerPOPath}$today/../*.lua ${cConf.workDir} $mainServerCredential${cConf.mainServerPOPath}$today/*.csv ${cConf.workDir}")

                logger.info("대상 압축")
                Utils.processRun(baseDirectoryToPath, "7za a -mx=7 $lang $baseDirectoryToPath/*.csv")
                Utils.processRun(baseDirectoryToPath, "7za a -mx=7 $dest $baseDirectoryToPath/*.lua")

                logger.info("SFX 생성")
                logger.info("cat ${cConf.workDir}/7zCon.sfx $lang")
                Utils.processRun(baseDirectoryToPath, "cat ${cConf.workDir}/7zCon.sfx $lang", ProcessBuilder.Redirect.to(Paths.get("$lang.exe").toFile()))
                Utils.processRun(baseDirectoryToPath, "cat ${cConf.workDir}/7zCon.sfx $dest", ProcessBuilder.Redirect.to(Paths.get("$dest.exe").toFile()))

                logger.info("기존 업로드된 목적파일 삭제")
                Utils.processRun(baseDirectoryToPath, "gsutil rm $bucket/lang*.exe")
                Utils.processRun(baseDirectoryToPath, "gsutil rm $bucket/dest*.exe")

                logger.info("목적파일 업로드")
                Utils.processRun(baseDirectoryToPath, "gsutil cp $lang.exe $bucket/")
                Utils.processRun(baseDirectoryToPath, "gsutil cp $dest.exe $bucket/")

                logger.info("버전 문서 생성")
                logger.info("echo ${Date().time}/$todayWithYear/${Utils.CRC32(Paths.get("$lang.exe"))}")
                Utils.processRun(baseDirectoryToPath, "echo ${Date().time}/$todayWithYear/${Utils.CRC32(Paths.get("$lang.exe"))}", ProcessBuilder.Redirect.to(baseDirectoryToPath.resolve("ver.html").toFile()))

                logger.info("버전 문서 업로드")
                logger.info("$scp ${baseDirectoryToPath.resolve("ver.html")} $mainServerCredential${cConf.mainServerVersionDocumentPath}")
                Utils.processRun(baseDirectoryToPath, "$scp ${baseDirectoryToPath.resolve("ver.html")} $mainServerCredential${cConf.mainServerVersionDocumentPath}")
            }

            System.exit(0)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(CompressServerMain::class.java)
        @JvmStatic
        fun main(@Suppress("UnusedMainParameter") args: Array<String>) {
            CompressServerMain().start()
        }
    }
}
