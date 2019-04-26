package org.metalscraps.eso.lang.client.clipboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.metalscraps.eso.lang.lib.bean.ID
import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.regex.Pattern

internal class ClipboardDataManager(private val manager: ClipboardManager) {

    private var dupCheck = "Hello World!"

    private fun removeDuplicateItem(list: ArrayList<ID>) {
        for (id in ArrayList(list)) {
            val dup = ArrayList<ID>()
            // 내용, toStringDefault 결과는 같지만 다른 객체.
            for (id2 in list) if (id.toString() == id2.toString() && id !== id2) dup.add(id2)
            for (x in dup) list.remove(x)
        }
    }

    @Synchronized
    @Throws(IOException::class, UnsupportedFlavorException::class)
    internal fun getContent(data: String) {

        if (data != dupCheck) dupCheck = data
        else return

        val m = IDPattern.matcher(data)
        val arrayList = ArrayList<ID>()
        while (m.find()) {
            val id = ID(m.group(1), m.group(2), m.group(3))
            try { logger.info(id.toString() + "\t\t" + getURL(id)) }
            catch (e: ID.NotFileNameHead) { logger.info("Not filename head $e ${e.message}"); continue }
            catch (e: Exception) { e.printStackTrace() }
            arrayList.add(id)
        }
        removeDuplicateItem(arrayList)

        if (arrayList.size == 1) openZanata(arrayList[0])
        else if (arrayList.size > 0) manager.updatePane(arrayList)
    }

    internal fun openZanata(id: ID) {
        try {
            val x = getURL(id)
            logger.trace("GOTO : $x")
            Desktop.getDesktop().browse(URI(x))
        } catch (ignored: ID.NotFileNameHead) {
        } catch (e: Exception) {
            logger.error(e.message + "/" + id.toString())
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun getURL(id: ID): String {
        val projectName = Utils.getProjectNameByDocument(id)
        val latestVersion = Utils.getLatestVersion(projectName)
        return AppConfig.ZANATA_DOMAIN + "webtrans/translate?iteration=$latestVersion&project=$projectName&locale=ko-KR&localeId=ko#view:doc;doc:${id.head};msgcontext:${id.body}-${id.tail}"
    }

    companion object {
        private val IDPattern = Pattern.compile("([a-zA-Z][a-zA-Z\\d-_,'()]+)[_-](\\d)[_-](\\d+)[_-]?")
        private val objectMapper = ObjectMapper().registerKotlinModule()
        private val logger = LoggerFactory.getLogger(ClipboardDataManager::class.java)
    }
}