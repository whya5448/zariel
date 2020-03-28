package org.metalscraps.eso.lang.client.clipboard

import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.LoggerFactory
import java.awt.Toolkit
import java.awt.datatransfer.*
import java.io.IOException

internal class ClipboardListener(private val manager: ClipboardManager) : FlavorListener {

    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    init {
        // Owner 뺏기
        val ss = StringSelection("")
        clipboard.setContents(ss, ss)
    }

    internal fun addClipboardListener() {
        try {
            Utils.getProjectMap()
        } catch (x: Exception) {
            logger.error(x.toString())
            logger.error("클립보드 리스너 작동 중지")
            return
        }

        Toolkit.getDefaultToolkit().systemClipboard.addFlavorListener(this)
        logger.info("클립보드 리스너 등록됨.")
    }

    override fun flavorsChanged(e: FlavorEvent) {
        val trans = clipboard.getContents(null)
        if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            val s = trans.getTransferData(DataFlavor.stringFlavor) as String
            val ss = StringSelection(s)
            clipboard.setContents(ss, ss)

            try {
                manager.getContent(s)
            } catch (e1: IOException) {
                e1.printStackTrace()
            } catch (e1: UnsupportedFlavorException) {
                e1.printStackTrace()
            } catch (e1: IllegalStateException) {
                try {
                    manager.getContent(s)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ClipboardListener::class.java)
    }
}