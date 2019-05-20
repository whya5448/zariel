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
    private final val ttc:Path

    init {
        vars.baseDir = config.workDir
        lang = vars.workDir.resolve("lang_${AppVariables.todayWithYear}.7z")
        dest = vars.workDir.resolve("destinations_${AppVariables.todayWithYear}.7z")
        ttc = vars.workDir.resolve("tamrielTradeCentre_${AppVariables.todayWithYear}.7z")
    }

    fun init() : Boolean {
        vars.run {
            for(x in dirs) if(Files.notExists(x)) Files.createDirectories(x)
            if(config.isLinux) if(Files.notExists(Paths.get("/root/.ssh/id_rsa"))) {
                logger.error("github id_rsa 존재하지 않음.")
                return false
            }
        }
        return true
    }

    override fun start() {
        vars.run {
            var error = false
            logger.info(dateTime.format(DateTimeFormatter.ofPattern("yy-MM-dd hh:mm:ss")) + " / 작업 시작")
            if(!init()) {
                logger.info("초기화 실패")
                System.exit(-1)
            }

            // 이전 데이터 삭제
            logger.info("이전 데이터 삭제")
            deleteOldData()
            logger.info("PO 다운로드")
            try { Utils.downloadPOs() } catch (e: Exception) { logger.error(e.toString()); e.printStackTrace(); error = true }

            addons()
            logger.info("LANG 생성")
            makeLANG()
            if(compress()) sfx()
            else logger.info("SFX 스킵")

            Utils.processRun(workDir, "echo ${Date().time}/$todayWithYear/${Utils.crc32(Paths.get("$lang.exe"))}", ProcessBuilder.Redirect.to(workDir.resolve("version").toFile()))
            if(!error || config.forceUpload) upload()
        }
    }

    private fun addons() {
        // 데스티네이션
        AddonManager().destination()
        AddonManager().destination("kb", beta = true)
        AddonManager().destination("tr", writeFileName = true)
        AddonManager().tamrielTradeCentre()
        AddonManager().tamrielTradeCentre("kb", beta = true)
        AddonManager().tamrielTradeCentre("tr", writeFileName = true)
    }

    private fun compress() : Boolean {
        System.gc() // 외부 프로세스 사용 시 jvm oom이 안뜨므로 명시적 gc
        val needSfx = Files.notExists(lang) or Files.notExists(dest) or Files.notExists(ttc)
        vars.run {
            logger.info("대상 압축")
            //Utils.kt.processRun(workDir, "7za a -m0=LZMA2:d96m:fb64 -mx=5 $lang $workDir/*.lang") // 최대압축/메모리 -1.5G, 아카이브 17mb
            if(Files.notExists(lang)) Utils.processRun(workDir, "7za a -mmt=1 -m0=LZMA2:d32m:fb64 -mx=5 $lang $workDir/*.lang") // 적당히, 메모리 380m, 아카이브 30m, only 1 threads.
            if(Files.notExists(dest)) Utils.processRun(workDir, "7za a -mx=9 $dest $workAddonDir/Destinations/*")
            if(Files.notExists(ttc)) Utils.processRun(workDir, "7za a -mx=9 $ttc $workAddonDir/TamrielTradeCentre/*")
        }
        if(!needSfx) logger.info("압축 스킵")
        return needSfx
    }

    private fun sfx() {
        vars.run {
            logger.info("SFX 생성")
            val sfx = javaClass.classLoader.getResource("./7zCon.sfx").path
            Utils.processRun(workDir, "cat $sfx $lang", ProcessBuilder.Redirect.to(Paths.get("$lang.exe").toFile()))
            Utils.processRun(workDir, "cat $sfx $dest", ProcessBuilder.Redirect.to(Paths.get("$dest.exe").toFile()))
            Utils.processRun(workDir, "cat $sfx $ttc", ProcessBuilder.Redirect.to(Paths.get("$ttc.exe").toFile()))
        }
    }

    private fun upload() {
        vars.run {
            logger.info("목적파일 업로드")
            // 버전 문서
            Utils.processRun(workDir, "chmod 600 /root/.ssh/id_rsa")
            Utils.processRun(workDir, "git init")
            Utils.processRun(workDir, "git add ${workDir.resolve("version")} $lang.exe $dest.exe $ttc.exe po")
            Utils.processRun(workDir, "git commit -m $todayWithYear")
            Utils.processRun(workDir, "git remote add origin git@github.com:Whya5448/EsoKR-LANG.git")
            Utils.processRun(workDir, "git push -u origin master --force")
            Utils.processRun(workDir, "rm .git -rf")
        }
    }

    private fun deleteOldData() {
        vars.run {
            val workDir = "$baseDir/$WORK_DIR_PREFIX"
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

    private fun makeLANG() {
        vars.run {
            var isNotExist = false
            for(x in arrayOf("kr.lang", "kb.lang", "tr.lang")) isNotExist = isNotExist || Files.notExists(workDir.resolve(x))
            if(isNotExist) {
                val list = Utils.getPOList(Utils.listFiles(poDir, "po"))

                if(Files.notExists(workDir.resolve("kr.lang"))) Utils.makeLANGwithLog(workDir.resolve("kr.lang"), list)
                if(Files.notExists(workDir.resolve("kb.lang"))) Utils.makeLANGwithLog(workDir.resolve("kb.lang"), list, beta = true)
                if(Files.notExists(workDir.resolve("tr.lang"))) Utils.makeLANGwithLog(workDir.resolve("tr.lang"), list, writeFileName = true)
            }
        }
    }
}
