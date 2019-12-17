package org.metalscraps.eso.lang.server

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class ServerApplication : CommandLineRunner {
    private final val logger = LoggerFactory.getLogger(ServerApplication::class.java)

    @Autowired
    lateinit var main: ServerMain

    override fun run(vararg args: String?) {
        logger.debug("args size : ${args.size}");
        logger.debug("args ======================")
        args.forEach { logger.info(it); }
        logger.debug("===========================")

        if (args.contains("now")) main.start()
        else logger.info("run at 03:00+09")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<ServerApplication>(*args)
        }
    }
}