package org.metalscraps.eso.lang.client.clipboard

import org.metalscraps.eso.lang.client.config.ClientConfig
import org.metalscraps.eso.lang.lib.bean.ID
import org.springframework.stereotype.Component

@Component
final class ClipboardManager(config: ClientConfig) {
    fun getContent(s: String) {
        return dataManager.getContent(s)
    }

    fun updatePane(arrayList: ArrayList<ID>) {
        gui.updatePane(arrayList)
    }

    fun openZanata(readValue: ID) {
        dataManager.openZanata(readValue)
    }

    fun addClipboardListener() {
        gui.show();listener.addClipboardListener()
    }

    private val gui = ClipboardListenerWindow(this, config)
    private val dataManager = ClipboardDataManager(this)
    private val listener = ClipboardListener(this)
}