package org.metalscraps.eso.lang.client

import org.metalscraps.eso.lang.client.clipboard.ClipboardManager
import org.metalscraps.eso.lang.client.config.ClientConfig
import org.metalscraps.eso.lang.client.config.ClientConfig.ClientConfigOptions.LOCAL_LANG_VERSION
import org.metalscraps.eso.lang.client.gui.OptionPanel
import org.metalscraps.eso.lang.lib.config.ESOMain
import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.awt.*
import java.io.IOException
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.function.BiPredicate


@Component
class ClientMain(private val config:ClientConfig, private val clipboardManager: ClipboardManager) : ESOMain {

    @Autowired lateinit var optionPanel: OptionPanel
    private val logger = LoggerFactory.getLogger(ClientMain::class.java)
    private var serverFileName: String? = null
    private var serverVer: Long = 0
    private var crc32: String? = null
    private var needUpdate = false
    private val localVer = config.localLangVersion
    private var sha = getSHA()

    private fun updateLocalConfig() {
        if (config.isUpdateLang) {
            config.put(LOCAL_LANG_VERSION, serverVer)
            config.store()
            logger.info("업데이트 성공")
        }

        val esoDir = Utils.getESOLangDir()
        try {
            if (Files.notExists(esoDir)) Files.createDirectories(esoDir)
            Files.find(config.appPath, 1, BiPredicate { x, attr -> attr.isRegularFile && x.toString().endsWith(".lang") }).forEach {
                try { Files.move(it, esoDir.resolve(it.fileName), StandardCopyOption.REPLACE_EXISTING) }
                catch (e: IOException) { e.printStackTrace() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getDataVersion() {

        logger.info("서버 버전 확인 중...")
        val ver = URL("https://raw.githubusercontent.com/Whya5448/EsoKR-LANG/master/version").readText()
        val resData = ver.trim { it <= ' ' }.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        serverVer = resData[0].toLong()
        serverFileName = resData[1]
        crc32 = resData[2]

        logger.info("서버 파일명 : $serverFileName")
        logger.info("서버 버전 : $serverVer")
        logger.info("로컬 버전 : $localVer")

        needUpdate = serverVer > localVer
        if (needUpdate) logger.info("업데이트 필요함.")
    }

    private fun update(): Boolean {
        config.run {

            if(!config.isUpdateLang) {
                logger.info("업데이트 설정 꺼져있음.")
                return true
            }

            val langPath = appPath.resolve("csv.exe")
            downloadCSVs(langPath)
            decompressCSVs(langPath)

        }

        return true
    }


    private fun decompressCSVs(langPath: Path) {
      config.run {
            try {
                Utils.processRun(config.appPath, "$langPath -y")
            } catch (e: InterruptedException) {
                e.printStackTrace()
                logger.error("언어파일 압축해제 실패")
                config.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.errCode)
            } catch (e: IOException) {
                e.printStackTrace()
                logger.error("언어파일 압축해제 실패")
                config.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.errCode)
            }

            if(config.isDeleteTemp) try { Files.deleteIfExists(langPath) } catch (ignored: IOException) {}
        }
    }


    private fun getSHA(): String {
        val x = URL("https://api.github.com/repos/Whya5448/EsoKR-LANG/git/refs/heads/master").readText()
        sha = x.substring( x.indexOf("sha")+6, x.indexOf("sha")+6+40)
        return sha
    }

    private fun downloadCSVs(csvPath: Path) {
        val request = HttpRequest.newBuilder().uri(URI.create("${ClientConfig.CDN}$sha/lang_$serverFileName.7z.exe")).build()

        if (Files.exists(csvPath) && Utils.crc32(csvPath).toString() == crc32) {
            logger.info("언어 파일 존재함. 다운로드 스킵")
            return
        }

        logger.info("CSV 다운로드 시도")
        try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofFile(csvPath))
        } catch (e: IOException) {
            logger.error("언어 다운로드 실패")
            e.printStackTrace()
            config.exit(AppErrorCode.CANNOT_DOWNLOAD_LANG.errCode)
        } catch (e: InterruptedException) {
            logger.error("언어 다운로드 실패")
            e.printStackTrace()
            config.exit(AppErrorCode.CANNOT_DOWNLOAD_LANG.errCode)
        } catch (e: Exception) {
            logger.error(e.message)
            e.printStackTrace()
        }
        logger.info("CSV 다운로드 성공")

        if (crc32 != Utils.crc32(csvPath).toString()) {
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
        val pop = PopupMenu()
        val settings = MenuItem("설정")
        settings.addActionListener { optionPanel.isVisible(true) }
        val exit = MenuItem("종료")
        exit.addActionListener { config.exit(0) }
        pop.add(settings)
        pop.addSeparator()
        pop.add(exit)

        val image = Toolkit.getDefaultToolkit().createImage(javaClass.classLoader.getResource("eso_256x256.png"))
        val trayIcon = TrayIcon(image)
        trayIcon.isImageAutoSize = true
        trayIcon.toolTip = "엘온갤 업데이터"
        trayIcon.popupMenu = pop
        try {
            val sysTray = SystemTray.getSystemTray()
            sysTray.trayIcons.iterator().forEach(sysTray::remove)
            sysTray.add(trayIcon)
        } catch (e: AWTException) {}
    }


    override fun start() {
        getDataVersion()
        if (needUpdate) if (update()) updateLocalConfig() else logger.error("업데이트 실패")
        else logger.info("최신 버전임")

        // 스팀 실행
        if(config.isLaunchAfterUpdate) Desktop.getDesktop().browse(URI("steam://rungameid/306130"))


        //트레이 아이콘 등록
        registerTrayIcon()

        // 클립보드 리스너 등록
        if(config.isEnableZanataListener) clipboardManager.addClipboardListener()
        else logger.info("클립보드 리스너 실행 안함.")
    }

}
