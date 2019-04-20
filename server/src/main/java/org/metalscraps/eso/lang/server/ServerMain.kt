package org.metalscraps.eso.lang.server

import org.metalscraps.eso.lang.lib.AddonManager
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.config.ESOMain
import org.metalscraps.eso.lang.lib.util.Utils
import org.metalscraps.eso.lang.server.config.ServerConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Predicate

@Component
class ServerMain(private val config:ServerConfig) : ESOMain {
    private final val logger = LoggerFactory.getLogger(ServerMain::class.java)
    private val vars = AppVariables
    private final val lang:Path
    private final val dest:Path

    init {
        vars.relocate(config.workDir)
        lang = vars.workDir.resolve("lang_${AppVariables.todayWithYear}.7z")
        dest = vars.workDir.resolve("destinations_${AppVariables.todayWithYear}.7z")
    }

    override fun start() {
        vars.run {
            logger.info(dateTime.format(DateTimeFormatter.ofPattern("yy-MM-dd hh:mm:ss")) + " / 작업 시작")
            if(Files.notExists(baseDir)) Files.createDirectories(baseDir)

            // 이전 데이터 삭제
            logger.info("이전 데이터 삭제")
            deletePO()
            logger.info("PO 다운로드")
            Utils.downloadPOs()
            logger.info("다운로드 된 PO 파일 문자셋 변경")
            Utils.convertKO_POtoCN()

            Addons()
            logger.info("CSV 생성")
            makeCSV()
            compress()
            sfx()
            //upload()
        }
    }

    private fun Addons() {
        // 데스티네이션
        AddonManager().destination()
    }

    private fun compress() {
        System.gc() // 외부 프로세스 사용 시 jvm oom이 안뜨므로 명시적 gc
        vars.run {
            logger.info("대상 압축")
            Utils.processRun(workDir, "7za a -m0=LZMA2:d96m:fb64 -mx=5 $lang $workDir/*.csv")
            Utils.processRun(workDir, "7za a -mx=9 $dest $baseDir/Addons/Destinations/*")
        }
    }

    private fun sfx() {
        vars.run {
            logger.info("SFX 생성")
            val sfx = javaClass.classLoader.getResource("./7zCon.sfx").path
            Utils.processRun(workDir, "cat $sfx $lang", ProcessBuilder.Redirect.to(Paths.get("$lang.exe").toFile()))
            Utils.processRun(workDir, "cat $sfx $dest", ProcessBuilder.Redirect.to(Paths.get("$dest.exe").toFile()))
        }
    }

    private fun upload() {
        vars.run {
            val bucket = "gs://eso-team-waldo-bucket"

            logger.info("기존 업로드된 목적파일 삭제")
            Utils.processRun(poDir, "gsutil rm $bucket/lang*.exe")
            Utils.processRun(poDir, "gsutil rm $bucket/dest*.exe")

            logger.info("목적파일 업로드")
            Utils.processRun(poDir, "gsutil cp $lang.exe $bucket/")
            Utils.processRun(poDir, "gsutil cp $dest.exe $bucket/")

            logger.info("버전 문서 생성")
            Utils.processRun(poDir, "echo ${Date().time}/$todayWithYear/${Utils.crc32(Paths.get("$lang.exe"))}", ProcessBuilder.Redirect.to(poDir.resolve("ver.html").toFile()))

            logger.info("버전 문서 업로드")
            //Utils.processRun(poDir, "$scp ${poDir.resolve("ver.html")} $mainServerCredential${config.mainServerVersionDocumentPath}")
        }
    }

    private fun deletePO() {
        vars.run {
            val workDir = "$baseDir/WORK_"
            val p = Predicate { x:Path -> x.toString().startsWith(workDir) && !x.toString().startsWith("$workDir$today") }
            try {
                // 디렉토리 사용중 오류, 파일 먼저 지우고 디렉토리 지우기
                Files.walk(baseDir).filter(p.and { x -> Files.isRegularFile(x) }).forEach(Files::delete)
                Files.walk(baseDir).filter(p.and { x -> Files.isDirectory(x) }).forEach(Files::delete)
            } catch (e: IOException) {
                logger.error(e.message + " 이전 파일 삭제 실패")
                e.printStackTrace()
            }
        }
    }

    private fun makeCSV() {
        vars.run {
            val listFiles = Utils.listFiles(poDir, "po2")
            val list = Utils.getMergedPO(listFiles)

            Utils.makeCSVwithLog(workDir.resolve("kr.csv"), list)
            Utils.makeCSVwithLog(workDir.resolve("kr_beta.csv"), list, beta = true)
            Utils.makeCSVwithLog(workDir.resolve("tr.csv"), list, writeFileName = true)
        }
    }
}
