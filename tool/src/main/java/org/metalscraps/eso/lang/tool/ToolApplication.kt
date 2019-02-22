package org.metalscraps.eso.lang.tool

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Created by 안병길 on 2018-01-17.
 * Whya5448@gmail.com
 */

@SpringBootApplication
internal class ToolApplication : CommandLineRunner {

    @Autowired lateinit var toolBody:ToolBody

    override fun run(args:Array<String>) {
        toolBody.start(args)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) { runApplication<ToolApplication>(*args) }
    }

}
