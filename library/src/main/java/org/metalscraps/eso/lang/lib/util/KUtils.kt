package org.metalscraps.eso.lang.lib.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.metalscraps.eso.lang.lib.bean.PO
import org.metalscraps.eso.lang.lib.bean.ToCSVConfig
import org.metalscraps.eso.lang.lib.bean.XPO
import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.metalscraps.eso.lang.lib.config.AppWorkConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.swing.filechooser.FileSystemView

class KUtils {

    companion object {
        private var vars: AppVariables = AppVariables
        val logger:Logger = LoggerFactory.getLogger(KUtils::class.java)

        /* ////////////////////////////////////////////////////
        // 데이터 핸들링
        */ ////////////////////////////////////////////////////

        fun KOToCN(string: String): String {
            val c = string.toCharArray()
            for (i in c.indices) if (c[i].toInt() in 0xAC00..0xEA00) c[i] = c[i] - 0x3E00
            return c.toString()
        } // KOToCN

        fun CNtoKO(string: String): String {
            val c = string.toCharArray()
            for (i in c.indices) if (c[i].toInt() in 0x6E00..0xAC00) c[i] = c[i] + 0x3E00
            return c.toString()
        } // CNtoKO

        private fun parseSourceToMap(path:Path): String {
            var source = ""
            try { source = Files.readString(path, AppConfig.CHARSET) }
            catch (e: IOException) {
                logger.error("파일 읽기 에러")
                e.printStackTrace()
                System.exit(0)
            }

            source = source.replace("\\\\\"".toRegex(), "\"\"") // \" 로 되어있는 쌍따옴표 이스케이프 변환 "" 더블-더블 쿼테이션으로 이스케이프 시켜야함.
                    .replace("\\\\\\\\".toRegex(), "\\\\") // 백슬래쉬 두번 나오는거 ex) ESOUI\\ABC\\DEF 하나로 고침.
            return source
        } // parseSourceToMap


        fun textParse(path:Path, keyGroup:Int = 2): MutableMap<String, XPO> {

            val poMap = HashMap<String, XPO>()
            val fileName = getName(path)
            val source = parseSourceToMap(path)

            val pattern = getPattern(path)
            val m = pattern.matcher(source)
            val isPOPattern = pattern == AppConfig.POPattern
            while (m.find()) {
                val po = XPO(id = m.group(2), source = m.group(6), target = m.group(7), fileName = fileName)
                if (isPOPattern && m.group(1) != null && m.group(1) == "#, fuzzy") po.isFuzzy = true
                poMap[m.group(keyGroup)] = po
            }

            return poMap
        } // textParse

        private fun getMergedPOtoMap(fileList: Collection<Path>): MutableMap<String, XPO> {
            val map = HashMap<String, XPO>()

            for (x in fileList) {
                val fileName: String = getName(x)

                // pregame 쪽 데이터
                if (fileName == "00_EsoUI_Client" || fileName == "00_EsoUI_Pregame") continue

                map.putAll(textParse(x))
                logger.trace(x.toString())
            }

            map.computeIfPresent("242841733-0-54340") { _, v -> v.target = Utils.KOToCN("매지카 물약"); v; }
            return map
        } // getMergedPOtoMap

        fun getMergedPO(fileList: MutableList<Path>): ArrayList<XPO> {
            val sourceList = ArrayList<XPO>(getMergedPOtoMap(fileList).values)
            Collections.sort(sourceList, XPO.comparator)
            return sourceList
        } // getMergedPO


        fun convertKO_POtoCN() {
            try {
                for (file in listFiles(vars.poDir, "po")) {
                    val po2 = Paths.get(file.toString() + "2")
                    if (!Files.exists(po2)) Files.writeString(po2, Utils.KOToCN(Files.readString(file, AppConfig.CHARSET)))
                }
            } catch (e: Exception) { e.printStackTrace() }

        } // convertKO_PO_to_CN

        fun makeCSV(path: Path, poList: MutableList<XPO>, writeSource:Boolean = false, writeFileName:Boolean = false, beta:Boolean = false) {
            val sb = StringBuilder("\"Location\",\"Source\",\"Target\"\n")
            for (p in poList) sb.append(p.toCSVFormat(writeSource, writeFileName, beta))

            try { Files.writeString(path, sb.toString(), AppConfig.CHARSET) }
            catch (e: IOException) { e.printStackTrace() }
        } // makeCSV

        /* ////////////////////////////////////////////////////
        // 데이터 핸들링 끝
        */ ////////////////////////////////////////////////////


        /* ////////////////////////////////////////////////////
        // Path/이름 제어
        */ ////////////////////////////////////////////////////

        private fun getPattern(path:Path): Pattern {
            val ext = KUtils.getExtension(path)
            if (ext == "po" || ext == "po2") return AppConfig.POPattern
            else if (ext == "csv") return AppConfig.CSVPattern
            else {
                logger.error("알 수 없는 패턴 ${path.toAbsolutePath()}")
                System.exit(0)
            }
            return AppConfig.CSVOffsetPattern
        }

        fun listFiles(path:Path, ext:String) : MutableList<Path> {
            try { return Files.list(path).filter { x -> !Files.isDirectory(x) && getExtension(x) == ext }.collect(Collectors.toList()) }
            catch (e: IOException) { e.printStackTrace() }
            return ArrayList()
        }

        fun getName(path: Path): String {
            if (Files.isDirectory(path)) logger.warn("파일 아님 ${path.toAbsolutePath()}")
            val x = path.fileName.toString()
            return x.substring(0, x.lastIndexOf('.'))
        }

        fun getExtension(path: Path): String {
            if (Files.isDirectory(path)) logger.warn("파일 아님 ${path.toAbsolutePath()}")
            val x = path.fileName.toString()
            return x.substring(x.lastIndexOf('.') + 1)
        }

        fun getESOLangDir(): Path {
            return FileSystemView.getFileSystemView().defaultDirectory.toPath().resolve("Elder Scrolls Online/live/AddOns/gamedata/lang")
        }

        fun getESODir(): Path {
            return FileSystemView.getFileSystemView().defaultDirectory.toPath().resolve("Elder Scrolls Online/")
        }


        /* ////////////////////////////////////////////////////
        // Path/이름 끝
        */ ////////////////////////////////////////////////////


        /* ////////////////////////////////////////////////////
        // 통신 부분
        */ ////////////////////////////////////////////////////

        private fun getDefaultRestClient(domain: String): HttpRequest {
            return HttpRequest.newBuilder().uri(URI.create(domain)).header("Accept", "application/json").build()
        } // getDefaultRestClient

        private fun getBodyFromHTTPsRequest(request: HttpRequest): JsonNode? {

            val client = HttpClient.newHttpClient()
            var response: HttpResponse<String>? = null

            try { response = client.send(request, HttpResponse.BodyHandlers.ofString()) }
            catch (e: IOException) { e.printStackTrace() }
            catch (e: InterruptedException) { e.printStackTrace() }

            var jsonNode: JsonNode? = null

            val body = response!!.body()
            logger.trace(body)

            try {
                jsonNode = ObjectMapper().readTree(body)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return jsonNode
        } // getBodyFromHTTPsRequest

        fun getFileNames(projectName: String): ArrayList<String> {
            val fileNames = ArrayList<String>()
            val request = getDefaultRestClient(AppConfig.ZANATA_DOMAIN + "rest/projects/p/" + projectName + "/iterations/i/" + Utils.getLatestVersion(projectName) + "/r")
            val jsonNode = getBodyFromHTTPsRequest(request)

            val it = jsonNode!!.elements()
            while (it.hasNext()) {
                val node = it.next()
                val Trim = node.get("name").toString().replace("^\"|\"$".toRegex(), "")
                fileNames.add(Trim)
            }

            return fileNames
        } // getFileNames

        fun downloadPOs() {
            val timeTaken = LocalTime.now()
            downloadPO("ESO-item")
            downloadPO("ESO-skill")
            downloadPO("ESO-system")
            downloadPO("ESO-book")
            downloadPO("ESO-story")
            logger.info("총 " + timeTaken.until(LocalTime.now(), ChronoUnit.SECONDS) + "초")
        } // downloadPOs

        private fun downloadPO(projectName: String) {
            var pPO: Path? = null

            try {
                val url = AppConfig.ZANATA_DOMAIN + "rest/file/translation/" + projectName + "/" + Utils.getLatestVersion(projectName) + "/ko/po?docId="
                val fileNames = Utils.getFileNames(projectName)
                val poDir = vars.poDir

                if (!Files.exists(poDir)) Files.createDirectories(poDir)

                for (fileName in fileNames) {

                    // 우리가 사용하는 데이터 아님.
                    if (fileName == "00_EsoUI_Client" || fileName == "00_EsoUI_Pregame") continue

                    val ltStart = LocalTime.now()
                    var fileURL = url + fileName
                    fileURL = fileURL.replace(" ", "%20")

                    pPO = poDir.resolve("$fileName.po")

                    logger.trace("download zanata file  [$fileName] to local [$pPO] ")
                    if (!Files.exists(pPO)) {
                        val server = Channels.newChannel(URL(fileURL).openStream())
                        val out = FileChannel.open(pPO, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
                        out.transferFrom(server, 0, java.lang.Long.MAX_VALUE)
                    }

                    val ltEnd = LocalTime.now()
                    logger.trace(" " + ltStart.until(ltEnd, ChronoUnit.SECONDS) + "초")
                }
            } catch (e: IOException) {
                if (e.message!!.contains("Premature EOF")) {
                    logger.warn("EOF 재시도")
                    if (Files.exists(pPO)) try { Files.delete(pPO!!) }
                    catch (e1: IOException) { }

                    try { Thread.sleep(1800000) }
                    catch (e1: InterruptedException) { e1.printStackTrace() }

                    downloadPO(projectName)
                } else e.printStackTrace()
            } catch (e: Exception) {
                logger.error(e.message)
                e.printStackTrace()
            }
        } // downloadPO

        fun CRC32(p: Path): Long {
            val crc = java.util.zip.CRC32()
            try { if (Files.notExists(p) || Files.size(p) <= 0) return -1 }
            catch (e: IOException) { e.printStackTrace() }

            val buffer = Files.readAllBytes(p)
            crc.update(buffer, 0, buffer.size)
            return crc.value
        } // CRC32

        /* ////////////////////////////////////////////////////
        // 통신 부분 끝
        */ ////////////////////////////////////////////////////

    } // companion object
}