package org.metalscraps.eso.lang.client.gui

import org.metalscraps.eso.lang.client.config.ClientConfig
import org.metalscraps.eso.lang.client.config.ClientConfig.ClientConfigOptions.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.*
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

@Component
class OptionPanel(config: ClientConfig) {

    private val logger: Logger = LoggerFactory.getLogger(OptionPanel::class.java)
    private val frame = Frame()

    init {
        val height = 242
        val width = 222
        val ss = Toolkit.getDefaultToolkit().screenSize
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                frame.isVisible = false
            }
        })
        frame.layout = FlowLayout()
        frame.isResizable = false
        frame.bounds = Rectangle(ss.width / 2 - width / 2, ss.height / 2 - height / 2, width, height)
        frame.title = "설정"

        val launchESO = Checkbox("패치 완료 후 ESO 자동 실행")
        launchESO.state = config.isLaunchAfterUpdate
        launchESO.addItemListener(OptionItemListener(config, LAUNCH_ESO_AFTER_UPDATE))
        logger.trace("LAUNCH_ESO_AFTER_UPDATE : $LAUNCH_ESO_AFTER_UPDATE / ${config.isLaunchAfterUpdate}")

        val doLang = Checkbox("LANG 업데이트")
        doLang.state = config.isUpdateLang
        doLang.addItemListener(OptionItemListener(config, UPDATE_LANG))
        logger.trace("UPDATE_LANG : $UPDATE_LANG / ${config.isUpdateLang}")

        val doDesti = Checkbox("데스티네이션 업데이트")
        doDesti.state = config.isUpdateDestination
        doDesti.addItemListener(OptionItemListener(config, UPDATE_DESTINATIONS))
        logger.trace("UPDATE_DESTINATIONS : $UPDATE_DESTINATIONS / ${config.isUpdateDestination}")

        val enableZanataClipboardListener = Checkbox("자나타 클립보드 리스너")
        enableZanataClipboardListener.state = config.isEnableZanataListener
        enableZanataClipboardListener.addItemListener(OptionItemListener(config, ENABLE_ZANATA_LISTENER))
        logger.trace("ENABLE_ZANATA_LISTENER : $ENABLE_ZANATA_LISTENER / ${config.isEnableZanataListener}")

        frame.add(launchESO)
        frame.add(doLang)
        frame.add(doDesti)
        frame.add(enableZanataClipboardListener)
        frame.add(Label("문의 : @Whya5448, @Harion01"))
        frame.add(Label("노예구함"))
        frame.add(Label("https://github.com/Whya5448/zariel"))

    }

    fun isVisible(b: Boolean) {
        frame.isVisible = b
    }

    private class OptionItemListener(private val cConf: ClientConfig, private val opt: ClientConfig.ClientConfigOptions) : ItemListener {
        override fun itemStateChanged(e: ItemEvent) {
            cConf.put(opt, e.stateChange == ItemEvent.SELECTED); cConf.store()
        }
    }

}