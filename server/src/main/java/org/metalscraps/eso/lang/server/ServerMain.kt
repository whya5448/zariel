package org.metalscraps.eso.lang.server

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.compute.Compute
import com.google.api.services.compute.model.Operation
import org.metalscraps.eso.lang.lib.AddonManager
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.config.ESOMain
import org.metalscraps.eso.lang.lib.util.Utils
import org.metalscraps.eso.lang.server.config.ServerConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.GeneralSecurityException
import java.time.format.DateTimeFormatter
import java.util.function.Predicate

@Component
class ServerMain(private val config:ServerConfig) : ESOMain {
    private val vars = AppVariables
    private val logger = LoggerFactory.getLogger(ServerMain::class.java)

    override fun start() {
        vars.run {
            logger.info(dateTime.format(DateTimeFormatter.ofPattern("yy-MM-dd hh:mm:ss")) + " / 작업 시작")
            baseDir = config.workDir
            poDir = baseDir.resolve("PO_$today")

            // 이전 데이터 삭제
            logger.info("이전 데이터 삭제")
            deletePO()
            logger.info("PO 다운로드")
            Utils.downloadPOs()
            logger.info("다운로드 된 PO 파일 문자셋 변경")
            Utils.convertKO_POtoCN()
            logger.info("CSV 생성")
            if(Files.notExists(poDir.resolve("po2.7z"))) Utils.processRun(baseDir, "7za a -mx=1 ${poDir.resolve("po2.7z")} $poDir/*.po2")

            // 데스티네이션
            AddonManager(vars).destination()

            logger.info("인스턴스 시작")
            val res = startCompressServer()
            logger.info(res?.status)
        }
    }

    private fun deletePO() {
        vars.run {
            val workDir = "$baseDir/PO_"
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

    private fun startCompressServer(): Operation? {

        try {
            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()

            var credential = GoogleCredential.fromStream(FileInputStream(config.gcpPermJsonPath.toFile()))
            if (credential.createScopedRequired()) credential = credential.createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))

            val computeService = Compute.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName("Google-ComputeSample/0.1")
                    .build()

            val request = computeService.instances().start(config.gcpProjectName, config.gcpProjectZone, config.gcpCompressServerInstanceName)
            return request.execute()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        }

        return null
    }

}
