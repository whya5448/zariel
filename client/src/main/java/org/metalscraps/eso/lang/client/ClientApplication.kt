package org.metalscraps.eso.lang.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ClientApplication : CommandLineRunner {

    @Autowired
    lateinit var main: ClientMain

    override fun run(vararg args: String?) {
        main.start()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("java.awt.headless", "false")
            runApplication<ClientApplication>(*args)
        }
    }
}