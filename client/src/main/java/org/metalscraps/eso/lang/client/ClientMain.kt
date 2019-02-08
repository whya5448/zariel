package org.metalscraps.eso.lang.client

import org.metalscraps.eso.lang.client.config.Options.*
import org.metalscraps.eso.lang.client.gui.ClipboardListener
import org.metalscraps.eso.lang.client.gui.OptionPanel
import org.metalscraps.eso.lang.lib.config.ESOConfig
import org.metalscraps.eso.lang.lib.config.ESOConfigOptions
import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.LoggerFactory
import java.awt.*
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.function.BiPredicate


private class ClientMain private constructor() {
    private val appPath = Paths.get(System.getenv("localappdata") + "/" + "dc_eso_client")
    private val configPath = appPath.resolve(".config")
    private val cConf = ESOConfig(appPath, configPath)
    private var serverFileName: String? = null
    private var serverVer: Long = 0
    private val localVer: Long
    private var crc32: String? = null
    private var needUpdate = false
    private val optionPanel: OptionPanel

    init {

        logger.info("설정 불러오기")
        cConf.load(mapOf(
                LANG_VERSION to 0,
                ZANATA_MANAGER_X to 0,
                ZANATA_MANAGER_Y to 0,
                ZANATA_MANAGER_WIDTH to 100,
                ZANATA_MANAGER_HEIGHT to 48,
                ZANATA_MANAGER_OPACITY to .5f,
                LAUNCH_ESO_AFTER_UPDATE to true,
                UPDATE_LANG to true,
                UPDATE_DESTINATIONS to true,
                ENABLE_ZANATA_LISTENER to true
        ).toMap(HashMap<ESOConfigOptions, Any>()))
        optionPanel = OptionPanel(cConf)
        localVer = cConf.getConf(LANG_VERSION).toLong()
    }

    private fun updateLocalConfig() {
        if (!isWhya) cConf.setProperty("ver", serverVer.toString())
        Utils.storeConfig(configPath, cConf)
        logger.info("업데이트 성공")

        val esoDir = Utils.getESOLangDir()
        try {
            if (Files.notExists(esoDir)) Files.createDirectories(esoDir)
            Files.find(appPath, 1, BiPredicate { _, attr -> attr.isRegularFile }).forEach { x ->
                try {
                    if (x.fileName.toString().endsWith(".csv")) Files.delete(x)
                    else if (x.fileName.toString().endsWith(".lang")) Files.move(x, esoDir.resolve(x.fileName), StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getDataVersion() {

        logger.info("서버 버전 확인 중...")
        var response: HttpResponse<String>? = null

        try {
            response = HttpClient.newHttpClient().send(HttpRequest.newBuilder().uri(URI.create("http://eso.metalscraps.org/ver.html")).GET().build(), HttpResponse.BodyHandlers.ofString())
        } catch (e: IOException) {
            logger.error("서버 버전 확인 실패")
            for (x in e.stackTrace) logger.error(x.toString())
            e.printStackTrace()
            cConf.exit(AppErrorCode.CANNOT_FIND_SERVER_VERSION.errCode)
        } catch (e: InterruptedException) {
            logger.error("서버 버전 확인 실패")
            for (x in e.stackTrace) logger.error(x.toString())
            e.printStackTrace()
            cConf.exit(AppErrorCode.CANNOT_FIND_SERVER_VERSION.errCode)
        } catch (e: Exception) {
            logger.debug(e.message)
            e.printStackTrace()
            cConf.exit(-1)
        }

        if (response!!.statusCode() != 200) {
            logger.error("서버 버전 확인 실패 / STATUS : " + response.statusCode())
            cConf.exit(AppErrorCode.CANNOT_FIND_SERVER_VERSION.errCode)
        }
        val resData = response.body().trim { it <= ' ' }.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        serverVer = java.lang.Long.parseLong(resData[0])
        serverFileName = resData[1]
        crc32 = resData[2]

        logger.info("서버 파일명 : " + serverFileName!!)
        logger.info("서버 버전 : $serverVer")
        logger.info("로컬 버전 : $localVer")

        needUpdate = serverVer > localVer
        if (needUpdate) logger.info("업데이트 필요함.")
    }

    private fun registClipboardListener() {
        Toolkit.getDefaultToolkit().systemClipboard.addFlavorListener(ClipboardListener(cConf))
    }

    private fun update(): Boolean {
        val tool = appPath.resolve("EsoExtractData v0.32/EsoExtractData.exe")
        if (Files.notExists(tool)) {
            logger.info("툴 존재하지 않음.")
            if (downloadTool()) logger.info("툴 전송 성공")
            else {
                logger.info("툴 전송 실패")
                cConf.exit(AppErrorCode.CANNOT_DOWNLOAD_TOOL.errCode)
            }
        }

        if(cConf.getConf(UPDATE_LANG).toBoolean()) {
            val langPath = appPath.resolve("csv.exe")
            downloadCSVs(langPath)
            decompressCSVs(langPath)
            try {
                Utils.processRun(appPath, "$tool -p -x kr.csv -o kr.lang")
                Utils.processRun(appPath, "$tool -p -x kr_beta.csv -o kr_beta.lang")
                Utils.processRun(appPath, "$tool -p -x tr.csv -o tr.lang")
            } catch (e: Exception) {
                logger.error("LANG 생성 실패")
                e.printStackTrace()
                cConf.exit(AppErrorCode.CANNOT_CREATE_LANG_USING_TOOL.errCode)
            }
        }
        return true
    }

    private fun decompressCSVs(langPath: Path) {
        try {
            Utils.processRun(appPath, "$langPath -y")
        } catch (e: InterruptedException) {
            e.printStackTrace()
            logger.error("언어파일 압축해제 실패")
            cConf.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.errCode)
        } catch (e: IOException) {
            e.printStackTrace()
            logger.error("언어파일 압축해제 실패")
            cConf.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.errCode)
        }

        if (!isWhya) try {
            Files.deleteIfExists(langPath)
        } catch (ignored: IOException) {
        }

    }

    private fun downloadCSVs(csvPath: Path) {

        val request = HttpRequest.newBuilder().uri(URI.create(cdn + "lang_$serverFileName.7z.exe")).build()

        if (Files.exists(csvPath) && Utils.CRC32(csvPath).toString() == crc32) {
            logger.info("언어 파일 존재함. 다운로드 스킵")
            return
        }

        logger.info("다운로드 시도")
        try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofFile(csvPath))
        } catch (e: IOException) {
            logger.error("언어 다운로드 실패")
            e.printStackTrace()
            cConf.exit(AppErrorCode.CANNOT_DOWNLOAD_LANG.errCode)
        } catch (e: InterruptedException) {
            logger.error("언어 다운로드 실패")
            e.printStackTrace()
            cConf.exit(AppErrorCode.CANNOT_DOWNLOAD_LANG.errCode)
        } catch (e: Exception) {
            logger.error(e.message)
            e.printStackTrace()
        }

        if (crc32 != Utils.CRC32(csvPath).toString()) {
            logger.warn("LANG 파일 CRC 불일치")
            try {
                Files.deleteIfExists(csvPath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            downloadCSVs(csvPath)
        }

    }

    private fun registerTrayIcon() {
        val image: Image = Toolkit.getDefaultToolkit().getImage("eso.ico")
        val trayIcon = TrayIcon(image)
        trayIcon.isImageAutoSize = true
        trayIcon.toolTip = "엘온갤 업데이터"
        val pop = PopupMenu()
        val settings = MenuItem("설정")
        settings.addActionListener { optionPanel.isVisible = true }
        val exit = MenuItem("종료")
        exit.addActionListener { cConf.exit(0) }
        pop.add(settings)
        pop.addSeparator()
        pop.add(exit)
        trayIcon.popupMenu = pop
        try {
            for (icon in SystemTray.getSystemTray().trayIcons) SystemTray.getSystemTray().remove(icon)
            SystemTray.getSystemTray().add(trayIcon)
        } catch (e: AWTException) {}
    }

    private fun downloadTool(): Boolean {
        logger.info("다운로드 시도")
        val request = HttpRequest.newBuilder().uri(URI.create(cdn + "EsoExtractData%20v0.32.exe")).build()
        val toolPath = appPath.resolve("tool.exe")

        try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofFile(toolPath))
        } catch (e: IOException) {
            e.printStackTrace()
            logger.error("툴 다운로드 실패")
            cConf.exit(AppErrorCode.CANNOT_DOWNLOAD_TOOL.errCode)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            logger.error("툴 다운로드 실패")
            cConf.exit(AppErrorCode.CANNOT_DOWNLOAD_TOOL.errCode)
        }

        try {
            Utils.processRun(appPath,  "$toolPath -y")
        } catch (e: InterruptedException) {
            e.printStackTrace()
            logger.error("툴 압축해제 실패")
            cConf.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.errCode)
        } catch (e: IOException) {
            e.printStackTrace()
            logger.error("툴 압축해제 실패")
            cConf.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.errCode)
        }

        try {
            Files.delete(toolPath)
            logger.info("임시파일 삭제 성공")
        } catch (e: IOException) {
            logger.warn("임시파일 삭제 실패")
        }

        return Files.exists(appPath.resolve("EsoExtractData v0.32/EsoExtractData.exe"))
    }

    companion object {
        private const val cdn = "https://storage.googleapis.com/dcinside-esok-cdn/"
        private val logger = LoggerFactory.getLogger(ClientMain::class.java)
        private val isWhya = System.getenv().getOrDefault("debug", "") == "whya5448"

        @Throws(InterruptedException::class)
        @JvmStatic
        fun main(@Suppress("UnusedMainParameter") args: Array<String>) {
            // 업데이트

            val main = ClientMain()
            main.getDataVersion()
            if (main.needUpdate) if (main.update()) main.updateLocalConfig() else logger.error("업데이트 실패")
            else logger.info("최신 버전임")

            if(!isWhya) if(main.cConf.getConf(LAUNCH_ESO_AFTER_UPDATE).toBoolean()) Desktop.getDesktop().browse(URI("steam://rungameid/306130"))
            //트레이 아이콘 등록
            main.registerTrayIcon()

            // 클립보드 리스너 등록
            if(main.cConf.getConf(ENABLE_ZANATA_LISTENER).toBoolean()) main.registClipboardListener()

            // 종료 전까지 대기
            Thread.sleep(java.lang.Long.MAX_VALUE)
        }
    }

}
