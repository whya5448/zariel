package org.metalscraps.eso.lang.client.gui

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.metalscraps.eso.lang.client.config.ClientConfig
import org.metalscraps.eso.lang.lib.bean.ID
import org.metalscraps.eso.lang.lib.config.AppConfig
import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.*
import java.awt.datatransfer.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.regex.Pattern

@Component
class ClipboardListener(private val config: ClientConfig) : FlavorListener {

    private val frame: Frame
    private val panel: Panel
    private val textField: TextField
    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    private var dupCheck = "Hello World!"

    private fun removeDuplicateItem(list: ArrayList<ID>) {
        for (id in ArrayList(list)) {
            val dup = ArrayList<ID>()
            // 내용, toStringDefault 결과는 같지만 다른 객체.
            for (id2 in list) if (id.toString() == id2.toString() && id !== id2) dup.add(id2)
            for (x in dup) list.remove(x)
        }
    }

    init {
        Utils.getProjectMap()

        // Owner 뺏기
        val ss = StringSelection("")
        clipboard.setContents(ss, ss)

        frame = Frame()
        frame.isAlwaysOnTop = true
        frame.isUndecorated = true
        frame.isResizable = false
        frame.layout = BorderLayout()
        frame.opacity = config.zanataManagerO
        frame.bounds = Rectangle(
                config.zanataManagerX, config.zanataManagerY,
                config.zanataManagerW, config.zanataManagerH
        )
        frame.addWindowListener(object : WindowAdapter() { override fun windowClosing(e: WindowEvent?) { config.exit(0) } })

        panel = Panel()
        panel.layout = GridLayout()
        textField = TextField()
        textField.addActionListener {
            var text = textField.text
            if (!text.endsWith("_")) text += "_"
            val m = IDPattern.matcher(text)
            val arrayList = ArrayList<ID>()
            while (m.find()) arrayList.add(ID(m.group(1), m.group(2), m.group(3)))
            removeDuplicateItem(arrayList)
            if (arrayList.size == 1) openZanata(arrayList[0])
            else if (arrayList.size > 0) updatePane(arrayList)
        }
        frame.add(panel, BorderLayout.NORTH)
        frame.add(textField, BorderLayout.SOUTH)
    }

    private fun updatePane(list: List<ID>) {
        frame.isVisible = false
        frame.removeAll()
        panel.removeAll()
        list.forEach {
            val button = Button()
            button.label = "$it"
            try { button.actionCommand = objectMapper.writeValueAsString(it) } catch (e: JsonProcessingException) { e.printStackTrace() }
            button.addActionListener { e -> try { openZanata(objectMapper.readValue<ID>(e.actionCommand, ID::class.java)) } catch (e1: IOException) { e1.printStackTrace() } }
            panel.add(button)
        }
        frame.add(panel, BorderLayout.NORTH)
        frame.add(textField, BorderLayout.SOUTH)
        frame.isVisible = true
    }

    override fun flavorsChanged(e: FlavorEvent) {

        try { getContent() }
        catch (e1: IOException) { e1.printStackTrace() }
        catch (e1: UnsupportedFlavorException) { e1.printStackTrace() }
        catch (e1: IllegalStateException) {
            try { getContent() } catch (ignored: Exception) {}
        }
    }

    @Synchronized
    @Throws(IOException::class, UnsupportedFlavorException::class)
    private fun getContent() {

        val trans = clipboard.getContents(null)
        if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {

            val s = trans.getTransferData(DataFlavor.stringFlavor) as String
            val ss = StringSelection(s)
            clipboard.setContents(ss, ss)

            if (s != dupCheck) dupCheck = s
            else return

            val m = IDPattern.matcher(s)
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
            else if (arrayList.size > 0) updatePane(arrayList)
        }
    }

    private fun openZanata(id: ID) {
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
        private val logger = LoggerFactory.getLogger(ClipboardListener::class.java)
    }
}