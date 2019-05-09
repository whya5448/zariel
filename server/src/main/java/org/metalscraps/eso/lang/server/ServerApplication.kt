package org.metalscraps.eso.lang.server

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServerApplication : CommandLineRunner {

    @Autowired lateinit var main:ServerMain

    override fun run(vararg args: String?) {
        main.start()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<ServerApplication>(*args)
        }
    }
}