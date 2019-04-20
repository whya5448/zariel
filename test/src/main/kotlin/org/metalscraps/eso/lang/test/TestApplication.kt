package org.metalscraps.eso.lang.test

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.spi.FileSystemProvider

@SpringBootApplication
class TestApplication : CommandLineRunner {


    override fun run(vararg args: String?) {
        val loader = javaClass.classLoader
        println("1 "+ loader.getResource("7zCon.sfx")?.path)
        println("3 "+ loader.getResource("./7zCon.sfx")?.path)
        val x = FileSystems.getDefault().provider()
        println(x)
        println(x.scheme)
        println(x.javaClass)
        println(FileSystemProvider.installedProviders())
        println(System.getProperty("os.name"))
        println(System.getProperty("os.arch"))
        println(System.getProperty("os.version"))

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("java.awt.headless", "false")
            runApplication<TestApplication>(*args)
        }
    }
}