package org.metalscraps.eso.lang.client.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Checkbox
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.Rectangle
import java.awt.event.ItemEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*

class OptionPanel(val properties:Properties) : Frame() {

    private val logger:Logger = LoggerFactory.getLogger(OptionPanel::class.java)

    init {
        val x = this
        addWindowListener(object : WindowAdapter() { override fun windowClosing(e: WindowEvent?) { x.isVisible = false } })
        layout = FlowLayout()
        isResizable = false
        bounds = Rectangle(200, 200, 200, 144)


        val launchESOafterUpdate = Checkbox("패치 완료 후 ESO 자동 실행")
        launchESOafterUpdate.state = properties.getProperty("launchESOafterUpdate") == "0"
        launchESOafterUpdate.addItemListener { properties.setProperty("launchESOafterUpdate", (it.stateChange == ItemEvent.SELECTED).toString()) }

        val doLangUpdate = Checkbox("LANG 업데이트")
        doLangUpdate.state = properties.getProperty("doLangUpdate") == "0"
        doLangUpdate.addItemListener { properties.setProperty("doLangUpdate", (it.stateChange == ItemEvent.SELECTED).toString()) }

        val doDestnationsUpdate = Checkbox("데스티네이션 업데이트")
        doDestnationsUpdate.state = properties.getProperty("doDestnationsUpdate") == "0"
        doDestnationsUpdate.addItemListener { properties.setProperty("doDestnationsUpdate", (it.stateChange == ItemEvent.SELECTED).toString()) }

        add(launchESOafterUpdate)
        add(doLangUpdate)
        add(doDestnationsUpdate)
    }

}