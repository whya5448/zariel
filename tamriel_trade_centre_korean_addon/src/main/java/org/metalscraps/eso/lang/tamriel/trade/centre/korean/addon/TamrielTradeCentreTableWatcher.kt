package org.metalscraps.eso.lang.tamriel.trade.centre.korean.addon

import java.io.IOException
import java.nio.file.*

/**
 * Created by 안병길 on 2018-01-26.
 * Whya5448@gmail.com
 */
internal object TamrielTradeCentreTableWatcher {
    private val ENTRY_DELETE = StandardWatchEventKinds.ENTRY_DELETE

    @JvmStatic
    fun main() {

        val base = Utils.getESOLangDir()
        val backupTable = base.resolveSibling("Table_KR.lua")

        try {

            val watcher = FileSystems.getDefault().newWatchService()
            base.register(watcher, ENTRY_DELETE)

            while (true) {
                val key: WatchKey
                try { key = watcher.take() } catch (ignored: InterruptedException) { return }

                for (event in key.pollEvents()) {
                    // get file name
                    val ev = event as WatchEvent<*>
                    val path = ev.context() as Path

                    if (event.kind() === ENTRY_DELETE && path.toString() == "ItemLookUpTable_KR.lua")
                        Files.copy(backupTable, path)
                }

                // IMPORTANT: The key must be reset after processed
                if (!key.reset()) break
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
