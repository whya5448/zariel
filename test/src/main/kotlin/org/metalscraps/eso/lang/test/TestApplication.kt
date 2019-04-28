package org.metalscraps.eso.lang.test

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestApplication : CommandLineRunner {


    override fun run(vararg args: String?) {

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("java.awt.headless", "false")
            runApplication<TestApplication>(*args)
        }
    }
}