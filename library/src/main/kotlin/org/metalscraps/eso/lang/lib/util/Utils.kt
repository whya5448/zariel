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
            var keyGroup:Int = 2
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

        private fun getStringFromPO(path: Path, toChineseOffset: Boolean): StringBuilder {
            val sb = StringBuilder(Files.readString(path, AppConfig.CHARSET))

            return when(toChineseOffset) {
                true -> sb.toChineseOffset()
                false -> sb
            }
        } // getStringFromPO

        private fun parsePO(m: Matcher, fileName: String, opt: TextParseOptions): PO {
            m.run {
                val id = group(2).split("-")
                var source = group(6)
                var target = group(7)
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

                source = source.replace("\\\"\\\"","\"").replace("\"\n\"", "").replace("\\\\n".toRegex(RegexOption.IGNORE_CASE),"\n")
                target = target.replace("\\\"\\\"","\"").replace("\"\n\"", "").replace("\\\\n".toRegex(RegexOption.IGNORE_CASE),"\n")

                if (target == "") target = source
                else if (source == "") source = target

                if(!opt.parseSource) source = ""


                return PO(
                        id1 = id[0].toInt(), id2 = id[1].toInt(), id3 = id[2].toInt(),
                        source = source, target = target,
                        fileName = fileName, isFuzzy = group(1) == "#, fuzzy"
                )
            }
        }

        private fun getPOtoMap(path:Path, opt: TextParseOptions): MutableMap<String, PO> {
            val poMap = HashMap<String, PO>()
            val source = getStringFromPO(path, opt.toChineseOffset)
            val fileName = path.fileName()

            AppConfig.POPattern.matcher(source).run {
                while (find()) {
                    if (opt.excludeId.any{ group(2).contains(it) }) continue
                    poMap[
                            if (opt.toLower) group(opt.keyGroup).toLowerCase()
                            else group(opt.keyGroup).toLowerCase()
                    ] = parsePO(this, fileName, opt)
                }
            }
            return poMap
        } // getPOtoMap

        fun getMergedPOtoMap(fileList: Collection<Path>, options: TextParseOptions = TextParseOptions()): MutableMap<String, PO> {
            val map = HashMap<String, PO>()

            for (x in fileList) {
                if (x.fileName() in arrayOf("00_EsoUI_Client", "00_EsoUI_Pregame")) continue

                map.putAll(getPOtoMap(x, options))
                logger.trace(x.toString())
            }

            //map.computeIfPresent("242841733-0-54340") { _, v -> v.target = "매지카 물약".toChineseOffset(); v; }
            return map
        } // getMergedPOtoMap

        fun getMergedPOtoList(fileList: MutableList<Path>, options: TextParseOptions = TextParseOptions()): MutableList<PO> {
            return ArrayList(getMergedPOtoMap(fileList, options).values)
        } // getMergedPOtoList

        public fun makeLANGwithLog(path:Path, poList: MutableList<PO>, writeFileName:Boolean = false, beta:Boolean = false) {
            val timeTaken = LocalTime.now()
            makeLANG(path, poList, writeFileName, beta)
            logger.info("${path.fileName} ${timeTaken.until(LocalTime.now(), ChronoUnit.SECONDS)}초")
        } // makeLANGwithLog

        fun makeLANG(path: Path, poList: MutableList<PO>, writeFileName:Boolean = false, beta:Boolean = false) {
            var offset = 0
            FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use {
                it.write(ByteBuffer.allocate(Int.SIZE_BYTES * 2).putInt(2).putInt(poList.size).flip())
                for (p in poList) {
                    it.write(ByteBuffer.allocate(16).putInt(p.id1).putInt(p.id2).putInt(p.id3).putInt(offset).flip())
                    offset += p.getLengthForLang(writeFileName, beta) + 1
                }
                for (p in poList) {
                    val x = p.getTextForLang(writeFileName, beta)
                    it.write(ByteBuffer.allocate(x.size + 1).put(x).put(0).flip())
                }
            }
        } // makeLANG

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


        fun downloadPOs() {
            val timeTaken = LocalTime.now()
            for(x in arrayOf("item","skill","system","book","story")) downloadPO("ESO-$x")
            logger.info("총 ${timeTaken.until(LocalTime.now(), ChronoUnit.SECONDS)}초")
        } // downloadPOs

        private fun downloadPO(projectName: String, remainFiles:MutableList<String> = mutableListOf()) {
            val url = "${AppConfig.ZANATA_DOMAIN}rest/file/translation/$projectName/${getLatestVersion(projectName)}/ko/po?docId="
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