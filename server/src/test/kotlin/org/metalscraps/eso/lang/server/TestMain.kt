package org.metalscraps.eso.lang.server

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.runApplication

class TestMain : CommandLineRunner {

    override fun run(vararg args: String?) {

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<TestMain>(*args)
        }
    }

}