package org.metalscraps.eso.lang.client.gui

import org.metalscraps.eso.lang.client.config.ClientConfig
import org.metalscraps.eso.lang.client.config.Options
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.*
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

@Suppress("ReplacePutWithAssignment")
class OptionPanel(private var cConf:ClientConfig) : Frame() {

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
        launchESO.addItemListener(OptionItemListener(cConf, Options.LAUNCH_ESO_AFTER_UPDATE))
        logger.trace("Options.LAUNCH_ESO_AFTER_UPDATE : " + Options.LAUNCH_ESO_AFTER_UPDATE + " / " + cConf.isLaunchAfterUpdate)

        val doLang = Checkbox("LANG 업데이트")
        doLang.state = cConf.isUpdateLang
        doLang.addItemListener(OptionItemListener(cConf, Options.UPDATE_LANG))
        logger.trace("Options.UPDATE_LANG : " + Options.UPDATE_LANG + " / " + cConf.isUpdateLang)

        val doDesti = Checkbox("데스티네이션 업데이트")
        doDesti.state = cConf.isUpdateDestination
        doDesti.addItemListener(OptionItemListener(cConf, Options.UPDATE_DESTINATIONS))
        logger.trace("Options.UPDATE_DESTINATIONS : " + Options.UPDATE_DESTINATIONS + " / " + cConf.isUpdateDestination)

        val enableZanataClipboardListener = Checkbox("자나타 클립보드 리스너")
        enableZanataClipboardListener.state = cConf.isEnableZanataListener
        enableZanataClipboardListener.addItemListener(OptionItemListener(cConf, Options.ENABLE_ZANATA_LISTENER))
        logger.trace("Options.ENABLE_ZANATA_LISTENER : " + Options.ENABLE_ZANATA_LISTENER + " / " + cConf.isEnableZanataListener)

        add(launchESO)
        add(doLang)
        add(doDesti)
        add(enableZanataClipboardListener)
        add(Label("문의 : @Whya5448, @Harion01"))
        add(Label("노예구함"))
        add(Label("https://github.com/Whya5448/zariel"))

    }

    private class OptionItemListener(private val cConf: ClientConfig, private val opt:Options) : ItemListener {
        override fun itemStateChanged(e: ItemEvent) { cConf.put(opt, e.stateChange == ItemEvent.SELECTED); cConf.store() }
    }

}