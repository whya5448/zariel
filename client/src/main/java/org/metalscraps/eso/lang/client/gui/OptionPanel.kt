package org.metalscraps.eso.lang.client.gui

import org.metalscraps.eso.lang.client.config.ClientConfig
import org.metalscraps.eso.lang.client.config.ClientConfig.ClientConfigOptions.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.*
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

@Suppress("ReplacePutWithAssignment")
class OptionPanel(cConf:ClientConfig) : Frame() {

    private val logger:Logger = LoggerFactory.getLogger(OptionPanel::class.java)

    init {
        val height = 222
        val width = 222
        val ss = Toolkit.getDefaultToolkit().screenSize
        val x = this
        addWindowListener(object : WindowAdapter() { override fun windowClosing(e: WindowEvent?) { x.isVisible = false } })
        layout = FlowLayout()
        isResizable = false
        bounds = Rectangle(ss.width/2 - width/2, ss.height/2 - height/2, width, height)
        title = "설정"

        val launchESO = Checkbox("패치 완료 후 ESO 자동 실행")
        launchESO.state = cConf.isLaunchAfterUpdate
        launchESO.addItemListener(OptionItemListener(cConf, LAUNCH_ESO_AFTER_UPDATE))
        logger.trace("LAUNCH_ESO_AFTER_UPDATE : " + LAUNCH_ESO_AFTER_UPDATE + " / " + cConf.isLaunchAfterUpdate)

        val doLang = Checkbox("LANG 업데이트")
        doLang.state = cConf.isUpdateLang
        doLang.addItemListener(OptionItemListener(cConf, UPDATE_LANG))
        logger.trace("UPDATE_LANG : " + UPDATE_LANG + " / " + cConf.isUpdateLang)

        val doDesti = Checkbox("데스티네이션 업데이트")
        doDesti.state = cConf.isUpdateDestination
        doDesti.addItemListener(OptionItemListener(cConf, UPDATE_DESTINATIONS))
        logger.trace("UPDATE_DESTINATIONS : " + UPDATE_DESTINATIONS + " / " + cConf.isUpdateDestination)

        val enableZanataClipboardListener = Checkbox("자나타 클립보드 리스너")
        enableZanataClipboardListener.state = cConf.isEnableZanataListener
        enableZanataClipboardListener.addItemListener(OptionItemListener(cConf, ENABLE_ZANATA_LISTENER))
        logger.trace("ENABLE_ZANATA_LISTENER : " + ENABLE_ZANATA_LISTENER + " / " + cConf.isEnableZanataListener)

        add(launchESO)
        add(doLang)
        add(doDesti)
        add(enableZanataClipboardListener)
        add(Label("문의 : @Whya5448, @Harion01"))
        add(Label("노예구함"))
        add(Label("https://github.com/Whya5448/zariel"))

    }

    private class OptionItemListener(private val cConf: ClientConfig, private val opt:ClientConfig.ClientConfigOptions) : ItemListener {
        override fun itemStateChanged(e: ItemEvent) { cConf.put(opt, e.stateChange == ItemEvent.SELECTED); cConf.store() }
    }

}