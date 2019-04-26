package org.metalscraps.eso.lang.client.clipboard

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.metalscraps.eso.lang.client.config.ClientConfig
import org.metalscraps.eso.lang.lib.bean.ID
import org.slf4j.LoggerFactory
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException

internal class ClipboardListenerWindow(private val manager: ClipboardManager, private val config: ClientConfig) {

    private val frame: Frame = Frame()
    private val panel: Panel
    private val textField: TextField
    private val objectMapper = ObjectMapper().registerKotlinModule()

    init {
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
            manager.getContent(text)
        }
        frame.add(panel, BorderLayout.NORTH)
        frame.add(textField, BorderLayout.SOUTH)
    }

    internal fun updatePane(list: List<ID>) {
        frame.isVisible = false
        frame.removeAll()
        panel.removeAll()
        list.forEach {
            val button = Button()
            button.label = "$it"
            try { button.actionCommand = objectMapper.writeValueAsString(it) } catch (e: JsonProcessingException) { e.printStackTrace() }
            button.addActionListener { e -> try { manager.openZanata(objectMapper.readValue<ID>(e.actionCommand, ID::class.java)) } catch (e1: IOException) { e1.printStackTrace() } }
            panel.add(button)
        }
        frame.add(panel, BorderLayout.NORTH)
        frame.add(textField, BorderLayout.SOUTH)
        frame.isVisible = true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ClipboardListenerWindow::class.java)
    }
}