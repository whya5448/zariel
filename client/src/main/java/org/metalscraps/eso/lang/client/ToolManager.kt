package org.metalscraps.eso.lang.client

import org.metalscraps.eso.lang.client.config.ClientConfig
import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class ToolManager(private val config: ClientConfig) {
    private val logger = LoggerFactory.getLogger(ToolManager::class.java)
    private val tool = config.appPath.resolve("EsoExtractData v0.32/EsoExtractData.exe")

    init {
        if (Files.notExists(tool)) {
            logger.info("툴 존재하지 않음.")
            if (downloadTool()) logger.info("툴 전송 성공")
            else {
                logger.info("툴 전송 실패")
                config.exit(AppErrorCode.CANNOT_DOWNLOAD_TOOL.errCode)
            }
        }
    }

    private fun downloadTool(): Boolean {
        logger.info("다운로드 시도")
        val request = HttpRequest.newBuilder().uri(URI.create("${ClientConfig.CDN}EsoExtractData%20v0.32.exe")).build()
        val toolPath = config.appPath.resolve("tool.exe")

        try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofFile(toolPath))
        } catch (e: IOException) {
            e.printStackTrace()
            logger.error("툴 다운로드 실패")
            config.exit(AppErrorCode.CANNOT_DOWNLOAD_TOOL.errCode)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            logger.error("툴 다운로드 실패")
            config.exit(AppErrorCode.CANNOT_DOWNLOAD_TOOL.errCode)
        }

        try {
            Utils.processRun(config.appPath,  "$toolPath -y")
        } catch (e: InterruptedException) {
            e.printStackTrace()
            logger.error("툴 압축해제 실패")
            config.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.errCode)
        } catch (e: IOException) {
            e.printStackTrace()
            logger.error("툴 압축해제 실패")
            config.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.errCode)
        }

        try {
            Files.delete(toolPath)
            logger.info("임시파일 삭제 성공")
        } catch (e: IOException) {
            logger.warn("임시파일 삭제 실패")
        }

        return Files.exists(config.appPath.resolve("EsoExtractData v0.32/EsoExtractData.exe"))
    }

    fun csvTolang(csv: Path, lang: Path = csv.resolveSibling("$csv.lang")) {
        Utils.processRun(config.appPath, "$tool -p -x $csv -o $lang")
    }

    fun langDiff() {
        arrayOf(Paths.get("D:/SteamLibrary/steamapps/common/Zenimax Online/The Elder Scrolls Online/depot/eso.mnf"))
                .filter { Files.exists(it) }

    }

}