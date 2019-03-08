package org.metalscraps.eso.lang.server.compress

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CompressServerApplication : CommandLineRunner {

    @Autowired lateinit var main: CompressServerMain

    override fun run(args:Array<String>) {
        main.start()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) { runApplication<CompressServerApplication>(*args) }
    }

}
