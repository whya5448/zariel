package org.metalscraps.eso.lang.server

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.compute.Compute
import com.google.api.services.compute.model.Operation
import org.metalscraps.eso.lang.lib.AddonManager
import org.metalscraps.eso.lang.lib.bean.ToCSVConfig
import org.metalscraps.eso.lang.lib.config.AppWorkConfig
import org.metalscraps.eso.lang.lib.config.ESOConfigOptions
import org.metalscraps.eso.lang.lib.util.Utils
import org.metalscraps.eso.lang.server.config.ServerConfig
import org.metalscraps.eso.lang.server.config.ServerConfig.ServerConfigOptions.*
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.GeneralSecurityException
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Predicate

internal class ServerMain {
    private val appWorkConfig = AppWorkConfig()
    private val sConf = ServerConfig(Paths.get("."), Paths.get(".config"))

    init {
        sConf.load(mapOf(
                GCP_PROJECT_NAME to "gcp_project_name",
                GCP_PROJECT_ZONE to "gcp_project_zone",
                GCP_COMPRESS_SERVER_INSTANCE_NAME to "gcp_compress_server_instance_name",
                GCP_PERM_JSON_PATH to "/path/to/gcp/perm/json",
                WORK_DIR to "."
        ).toMap(HashMap<ESOConfigOptions, Any>()))
    }

    private fun run() {

        logger.info(appWorkConfig.dateTime.format(DateTimeFormatter.ofPattern("yy-MM-dd hh:mm:ss")) + " / 작업 시작")
        appWorkConfig.baseDirectoryToPath = sConf.workDir
        appWorkConfig.poDirectoryToPath = appWorkConfig.baseDirectoryToPath.resolve("PO_" + appWorkConfig.today)

        // 이전 데이터 삭제
        logger.info("이전 데이터 삭제")
        deletePO()
        logger.info("PO 다운로드")
        Utils.downloadPOs(appWorkConfig)
        logger.info("다운로드 된 PO 파일 문자셋 변경")
        Utils.convertKO_PO_to_CN(appWorkConfig)
        logger.info("CSV 생성")
        makeCSV()

        // 데스티네이션
        AddonManager(appWorkConfig).destination()

        logger.info("인스턴스 시작")
        val res = startCompressServer()
        logger.info(Objects.requireNonNull<Operation>(res).status)
    }

    private fun deletePO() {
        appWorkConfig.run {
            val workDir = baseDirectoryToPath + "/PO_"
            val p = Predicate { x:Path -> x.toString().startsWith("$workDir") && !x.toString().startsWith("$workDir$today") }
            try {
                // 디렉토리 사용중 오류, 파일 먼저 지우고 디렉토리 지우기
                Files.walk(baseDirectoryToPath).filter(p.and { x -> Files.isRegularFile(x) }).forEach(Files::delete)
                Files.walk(baseDirectoryToPath).filter(p.and { x -> Files.isDirectory(x) }).forEach(Files::delete)
            } catch (e: IOException) {
                logger.error(e.message + " 이전 파일 삭제 실패")
                e.printStackTrace()
            }
        }
    }

    private fun makeCSV() {
        val listFiles = Utils.listFiles(appWorkConfig.poDirectoryToPath, "po2")
        val list = Utils.getMergedPO(listFiles)
        val config = ToCSVConfig().setWriteSource(false)

        Utils.makeCSVwithLog(appWorkConfig.poDirectoryToPath.resolve("kr.csv"), config, list)
        Utils.makeCSVwithLog(appWorkConfig.poDirectoryToPath.resolve("kr_beta.csv"), config.setBeta(true), list)
        Utils.makeCSVwithLog(appWorkConfig.poDirectoryToPath.resolve("tr.csv"), config.setWriteFileName(true).setBeta(false), list)
    }

    private fun startCompressServer(): Operation? {

        try {
            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()

            var credential = GoogleCredential.fromStream(FileInputStream(sConf.gcpPermJsonPath.toFile()))
            if (credential.createScopedRequired()) credential = credential.createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))

            val computeService = Compute.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName("Google-ComputeSample/0.1")
                    .build()

            val request = computeService.instances().start(sConf.gcpProjectName, sConf.gcpProjectZone, sConf.gcpCompressServerInstanceName)
            return request.execute()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {

        private val logger = LoggerFactory.getLogger(ServerMain::class.java)

        @JvmStatic
        fun main(@Suppress("UnusedMainParameter") args: Array<String>) {
            ServerMain().run()
        }
    }
}
