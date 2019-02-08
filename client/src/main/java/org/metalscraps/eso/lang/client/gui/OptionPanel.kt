package org.metalscraps.eso.lang.client.gui

import org.metalscraps.eso.lang.client.config.Options
import org.metalscraps.eso.lang.lib.config.ESOConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.*
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

@Suppress("ReplacePutWithAssignment")
class OptionPanel(private val cConf:ESOConfig) : Frame() {

    private val logger:Logger = LoggerFactory.getLogger(OptionPanel::class.java)

    init {
        val ss = Toolkit.getDefaultToolkit().screenSize
        val x = this
        addWindowListener(object : WindowAdapter() { override fun windowClosing(e: WindowEvent?) { x.isVisible = false } })
        layout = FlowLayout()
        isResizable = false
        bounds = Rectangle(ss.width/2 - 200/2, ss.height/2 - 144/2, 200, 144)

        val launchESO = Checkbox("패치 완료 후 ESO 자동 실행")
        launchESO.state = cConf.getConf(Options.LAUNCH_ESO_AFTER_UPDATE).toBoolean()
        launchESO.addItemListener { ItemListenerWithStore(cConf, Options.LAUNCH_ESO_AFTER_UPDATE) }
        logger.trace("Options.LAUNCH_ESO_AFTER_UPDATE : " + Options.LAUNCH_ESO_AFTER_UPDATE + " / " + cConf.getConf(Options.LAUNCH_ESO_AFTER_UPDATE).toBoolean())

        val doLang = Checkbox("LANG 업데이트")
        doLang.state = cConf.getConf(Options.UPDATE_LANG).toBoolean()
        doLang.addItemListener { ItemListenerWithStore(cConf, Options.UPDATE_LANG) }
        logger.trace("Options.UPDATE_LANG : " + Options.UPDATE_LANG + " / " + cConf.getConf(Options.UPDATE_LANG).toBoolean())

        val doDesti = Checkbox("데스티네이션 업데이트")
        doDesti.state = cConf.getConf(Options.UPDATE_DESTINATIONS).toBoolean()
        doDesti.addItemListener { ItemListenerWithStore(cConf, Options.UPDATE_DESTINATIONS) }
        logger.trace("Options.UPDATE_DESTINATIONS : " + Options.UPDATE_DESTINATIONS + " / " + cConf.getConf(Options.UPDATE_DESTINATIONS).toBoolean())

        add(launchESO)
        add(doLang)
        add(doDesti)
    }

    private class ItemListenerWithStore(private val xConf: ESOConfig, private val opt:Options) : ItemListener {
        override fun itemStateChanged(e: ItemEvent) { xConf.put(opt, e.stateChange == ItemEvent.SELECTED); xConf.store() }
    }

}