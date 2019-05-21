@file:Suppress("RedundantVisibilityModifier")

package org.metalscraps.eso.lang.lib.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.metalscraps.eso.lang.lib.bean.ID
import org.metalscraps.eso.lang.lib.bean.PO
import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.config.AppVariables
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.regex.Matcher
import java.util.stream.Collectors
import java.util.zip.CRC32
import kotlin.collections.ArrayList

class Utils {
    data class TextParseOptions(
            var toChineseOffset: Boolean = true,
            var toLower: Boolean = false,
            var parseSource: Boolean = false,
            var excludeId: Array<Regex> = arrayOf(),
            var keyGroup:Int = 3
    )

    companion object {

        private val versionMap = mutableMapOf<String, String>()
        private val projectMap = mutableMapOf<String, MutableList<String>>(
                "ESO-skill" to mutableListOf(),
                "ESO-system" to mutableListOf(),
                "ESO-item" to mutableListOf(),
                "ESO-story" to mutableListOf(),
                "ESO-book" to mutableListOf()
        )
        private val logger:Logger = LoggerFactory.getLogger(Utils::class.java)
        private val vars: AppVariables = AppVariables

        /* ////////////////////////////////////////////////////
        // 데이터 핸들링
        */ ////////////////////////////////////////////////////

        private fun getStringBuilderFromPO(path: Path, toChineseOffset: Boolean): StringBuilder {

            // 자나타 개행 제거, "" 이스케이프 복원
            val sb = StringBuilder(Files.readString(path, AppConfig.CHARSET).replace("\"\$(\\r\\n|\\r|\\n)^\"".toRegex(RegexOption.MULTILINE), ""))

            var idx = sb.indexOf("\\\"\\\"")
            while(idx != -1) {
                sb.replace(idx, idx+4, "\"")
                idx = sb.indexOf("\\\"\\\"", idx)
            }

            //Files.writeString(path.resolveSibling("${path.fileName()}.mod.po"), sb.toString(), AppConfig.CHARSET)

            return when(toChineseOffset) {
                true -> sb.toChineseOffset()
                false -> sb
            }
        } // getStringBuilderFromPO

        private fun parsePO(m: Matcher, fileName: String, opt: TextParseOptions): PO {
            m.run {
                val id = group(3).split("-")
                var source = group(6)
                var target = group(9)
                if(opt.toLower) {
                    source = source.toLowerCase()
                    target = target.toLowerCase()
                }

                /*
                msgstr ""
                "이 귀족 은행원은 필요하면 어디든지 동행하며 모든 개인 은행 서비스를 제공해줄 수 있습니다. 이 친구에게는 너무 위험한 시로딜이나 "
                "전장같은 장소를 제외하면 말이죠.\n"
                "\n"
                "소환하면 당신과 그룹 멤버들이 서비스를 이용할 수 있습니다."
                */

                /*
                이 귀족 은행원은 필요하면 어디든지 동행하며 모든 개인 은행 서비스를 제공해줄 수 있습니다. 이 친구에게는 너무 위험한 시로딜이나 전장같은 장소를 제외하면 말이죠.

                소환하면 당신과 그룹 멤버들이 서비스를 이용할 수 있습니다.
                */

                // 자나타 포맷 버그. 자나타 내에선 개행 없으나 po 다운로드시 생김.
                // 진짜 개행 데이터 \r \n 문자로 표기되있음. → \r 안쓰는거같음. 확인필요
                // \"\" -> ", "\n" -> ""
                source = source.replace("\\\\n".toRegex(RegexOption.IGNORE_CASE),"\n")
                target = target.replace("\\\\n".toRegex(RegexOption.IGNORE_CASE),"\n")

                if (target == "") target = source
                else if (source == "") source = target

                val isFuzzy = group(1) == "#, fuzzy" || target.contains("-G-")
                if(!opt.parseSource && !isFuzzy) source = ""


                return PO(
                        id1 = id[0].toInt(), id2 = id[1].toInt(), id3 = id[2].toInt(),
                        source = source, target = target,
                        fileName = fileName, isFuzzy = isFuzzy
                )
            }
        }

        private fun getPO(path:Path, opt: TextParseOptions): MutableMap<String, PO> {
            val poMap = HashMap<String, PO>()
            val source = getStringBuilderFromPO(path, opt.toChineseOffset)
            val fileName = path.fileName()

            AppConfig.POPattern.matcher(source).run {
                while (find()) {
                    if (opt.excludeId.any{ group(2).contains(it) }) continue
                    poMap[
                            if (opt.toLower) group(opt.keyGroup).toLowerCase()
                            else group(opt.keyGroup)
                    ] = parsePO(this, fileName, opt)
                }
            }
            return poMap
        } // getPO

        fun getPOMap(fileList: Collection<Path>, options: TextParseOptions = TextParseOptions()): MutableMap<String, PO> {
            val map = HashMap<String, PO>()

            fileList.parallelStream().forEach {
                if (it.fileName() in arrayOf("00_EsoUI_Client", "00_EsoUI_Pregame")) return@forEach

                map.putAll(getPO(it, options))
                logger.trace(it.toString())
            }

            //map.computeIfPresent("242841733-0-54340") { _, v -> v.target = "매지카 물약".toChineseOffset(); v; }
            return map
        } // getPOMap

        fun getPOList(fileList: List<Path>, options: TextParseOptions = TextParseOptions()): MutableList<PO> {
            return ArrayList(getPOMap(fileList, options).values)
        } // getPOList

        public fun makeLANGwithLog(path:Path, poList: List<PO>, writeFileName:Boolean = false, beta:Boolean = false) {
            val timeTaken = LocalTime.now()
            makeLANG(path, poList, writeFileName, beta)
            logger.info("${path.fileName} ${timeTaken.until(LocalTime.now(), ChronoUnit.SECONDS)}초")
        } // makeLANGwithLog

        fun makeLANG(path: Path, poList: List<PO>, writeFileName:Boolean = false, beta:Boolean = false) {
            var offset = 0
            FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use { fc ->
                fc.write(ByteBuffer.allocate(Int.SIZE_BYTES * 2).putInt(2).putInt(poList.size).flip())
                poList.forEach {
                    fc.write(ByteBuffer.allocate(16).putInt(it.id1).putInt(it.id2).putInt(it.id3).putInt(offset).flip())
                    offset += it.getLengthForLang(writeFileName, beta) + 1
                }
                poList.forEach {
                    val x = it.getTextForLang(writeFileName, beta)
                    fc.write(ByteBuffer.allocate(x.size + 1).put(x).put(0).flip())
                }
            }

        } // makeLANG


        fun readLANG(path: Path): MutableList<PO> {
            val nil = 0.toByte()
            val list:ArrayList<PO> = ArrayList()

            fun parseBuffer(data: ByteArray, offset: Int, fileSize: Long): String {
                var res = ""

                if (offset >= fileSize) return res
                var i = 0

                while(data[offset+i] != nil) i++

                res = data.copyOfRange(offset, offset+i).toString(AppConfig.CHARSET)

                return res
            }

            val TEXT_RECORD_SIZE = 16
            val data = Files.readAllBytes(path)
            val fileSize = Files.size(path)
            //val fileID = copyInt(data, 0)
            val recordCnt = data.copyInt(4)

            val startTextOffset = recordCnt * 16 + 8
            for (i in 0 until recordCnt) {
                val recordOffset = 8 + i * TEXT_RECORD_SIZE

                val id = data.copyInt(recordOffset)
                val body = data.copyInt(recordOffset+4)
                val index = data.copyInt(recordOffset+8)
                val offset = data.copyInt(recordOffset+12) + startTextOffset

                if (offset < fileSize) {
                    //std::string Temp = ParseBufferString(pData, TextOffset, (size_t)Size);
                    //Record.Text = ReplaceStrings(ReplaceStrings(Temp, "\x0d", "\\r"), "\x0a", "\\n");
                    val text = parseBuffer(data, offset, fileSize)
                    list.add( PO(id1 = id, id2 = body, id3 = index, source = text, target = text) )
                } else logger.error("Warning: Read passed end of file (offset $offset) in text record $i")
            }
            return list
        } // readLANG

        /* ////////////////////////////////////////////////////
        // 데이터 핸들링 끝
        */ ////////////////////////////////////////////////////


        /* ////////////////////////////////////////////////////
        // Path 제어
        */ ////////////////////////////////////////////////////

        fun listFiles(path:Path = vars.poDir, ext:String = "po") : MutableList<Path> {
            try { return Files.list(path).filter { x -> !Files.isDirectory(x) && x.ext() == ext }.collect(Collectors.toList()) }
            catch (e: IOException) { e.printStackTrace() }
            return ArrayList()
        }

        fun getESOLangDir(): Path { return getESODir().resolve("live/addOns/gamedata/lang") }
        fun getESODir(): Path { return Paths.get(System.getProperty("user.home")).resolve("Documents/Elder Scrolls Online/") }


        /* ////////////////////////////////////////////////////
        // Path 끝
        */ ////////////////////////////////////////////////////


        /* ////////////////////////////////////////////////////
        // 통신 부분
        */ ////////////////////////////////////////////////////

        fun getDefaultRestClient(domain: String): HttpRequest {
            return HttpRequest.newBuilder().uri(URI.create(domain)).header("Accept", "application/json").build()
        } // getDefaultRestClient

        private fun getBodyFromHTTPsRequest(request: HttpRequest): JsonNode {

            val client = HttpClient.newHttpClient()
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            val body = response.body()
            logger.trace(body)

            return ObjectMapper().readTree(body)
        } // getBodyFromHTTPsRequest

        fun getLatestVersion(projectName: String): String {
            if (versionMap.containsKey(projectName)) return versionMap.getValue(projectName)
            val request = getDefaultRestClient("${AppConfig.ZANATA_DOMAIN}rest/projects/p/$projectName")

            val jsonNode = getBodyFromHTTPsRequest(request)
            var serverVer: Array<String>? = null

            for (node in jsonNode.get("iterations")) {
                if (node.get("status").toString().equals("\"ACTIVE\"", ignoreCase = true)) { // && temp > version
                    val tempVer = node.get("id").asText().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (serverVer == null) {
                        serverVer = tempVer
                        continue
                    }

                    val length = Math.max(tempVer.size, serverVer.size)
                    for (i in 0..length) {
                        val temp = if (i < tempVer.size) Integer.parseInt(tempVer[i]) else 0
                        val serv = if (i < serverVer!!.size) Integer.parseInt(serverVer[i]) else 0
                        if (temp < serv) break
                        if (temp > serv) serverVer = tempVer
                    }
                }
            }
            if (serverVer == null) serverVer = arrayOf("0.0.0")
            versionMap[projectName] = serverVer.joinToString(".")
            return versionMap.getValue(projectName)
        } // getLatestVersion

        private fun getFileNames(projectName: String): MutableList<String> {
            val request = getDefaultRestClient("${AppConfig.ZANATA_DOMAIN}rest/projects/p/$projectName/iterations/i/${getLatestVersion(projectName)}/r")
            return getBodyFromHTTPsRequest(request).map { it.findValue("name").toString().removeSurrounding("\"") }.toMutableList()
        } // getFileNames


        fun downloadPOs(language: String = "ko") {
            val timeTaken = LocalTime.now()
            for(x in arrayOf("item","skill","system","book","story")) downloadPO("ESO-$x", language = language)
            logger.info("총 ${timeTaken.until(LocalTime.now(), ChronoUnit.SECONDS)}초")
        } // downloadPOs

        private fun downloadPO(projectName: String, language: String = "ko", remainFiles:MutableList<String> = mutableListOf()) {
            val url = "${AppConfig.ZANATA_DOMAIN}rest/file/translation/$projectName/${getLatestVersion(projectName)}/$language/po?docId="
            val fileNames = if(remainFiles.size == 0) getFileNames(projectName) else remainFiles
            val poDir = vars.poDir

            if (!Files.exists(poDir)) Files.createDirectories(poDir)
            // 자나타 API 리밋
            ForkJoinPool(2).submit {
                fileNames.parallelStream().forEach {
                    val ltStart = LocalTime.now()
                    var fileURL = url + it
                    fileURL = fileURL.replace(" ", "%20")

                    val pPO = poDir.resolve("$it.po")
                    if (!Files.exists(pPO)) {
                        try {
                            val server = Channels.newChannel(URL(fileURL).openStream())
                            val out = FileChannel.open(pPO, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
                            out.transferFrom(server, 0, Long.MAX_VALUE)
                        } catch (e: IOException) {
                            val msg = e.message as String
                            if(msg.contains("response code: 429") || msg.contains("Premature EOF") || msg.contains("connection closed locally")) {
                                remainFiles.add(it)
                                logger.warn("오류, 재시도 $it $msg")
                                if (Files.exists(pPO)) try { Files.delete(pPO) } catch (e1: IOException) { }
                            } else e.printStackTrace()
                        } catch (e: Exception) {
                            logger.error(e.message)
                            e.printStackTrace()
                        }
                    }
                    val ltEnd = LocalTime.now()
                    logger.debug("downloaded zanata file  [$it] to local [$pPO] ${ltStart.until(ltEnd, ChronoUnit.SECONDS)}초")
                }
            }.get()

            if(remainFiles.size > 0) {
                Thread.sleep(5 * 1000)
                downloadPO(projectName)
            }
        } // downloadPO

        fun getProjectMap(): MutableMap<String, MutableList<String>> {
            projectMap.filter { it.value.size == 0 }.forEach { it.value.addAll(getFileNames(it.key)) }
            return projectMap
        }

        @Throws(Exception::class)
        fun getProjectNameByDocument(id: ID): String {
            if (!id.isFileNameHead()) throw ID.NotFileNameHead()
            getProjectMap().forEach { (pName, docs) ->
                docs.filter { it.equals(id.head, false) }.takeIf { it.isNotEmpty() }?.run {
                    id.head = first()
                    return pName
                }
            }
            throw Exception("프로젝트 못찾음 /$id")
        }

        fun crc32(p: Path): Long {
            val crc = CRC32()
            try { if (Files.notExists(p) || Files.size(p) <= 0) return -1 }
            catch (e: IOException) { e.printStackTrace() }

            val buffer = Files.readAllBytes(p)
            crc.update(buffer, 0, buffer.size)
            return crc.value
        } // crc32

        /* ////////////////////////////////////////////////////
        // 통신 부분 끝
        */ ////////////////////////////////////////////////////

        /* ////////////////////////////////////////////////////
        // 프로세스 러너
        */ ////////////////////////////////////////////////////

        @Throws(IOException::class, InterruptedException::class)
        fun processRun(baseDirectory: Path, command: String) { processRun(baseDirectory, command, ProcessBuilder.Redirect.INHERIT) }

        @Throws(IOException::class, InterruptedException::class)
        fun processRun(baseDirectory: Path, command: String, redirect: ProcessBuilder.Redirect) {
            logger.debug(command)
            val pb = ProcessBuilder()
                    .directory(baseDirectory.toFile())
                    .command(*command.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(redirect)
            pb.start().waitFor()
        }

        /* ////////////////////////////////////////////////////
        // 프로세스 러너 끝
        */ ////////////////////////////////////////////////////

    } // companion object
}