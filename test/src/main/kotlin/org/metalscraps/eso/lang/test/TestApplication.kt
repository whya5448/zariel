package org.metalscraps.eso.lang.test

import org.metalscraps.eso.lang.lib.util.Utils
import org.metalscraps.eso.lang.lib.util.fileName
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.*

@SpringBootApplication
class TestApplication : CommandLineRunner {


    override fun run(vararg args: String?) {
/*
        for(dir in arrayOf(Paths.get("C:\\Users\\Whya5\\IdeaProjects\\EsoKR-Translate\\translate\\ko"), Paths.get("C:\\Users\\Whya5\\IdeaProjects\\EsoKR-Translate\\translate\\ja"))) {
            println("=================================")
            val list = Utils.listFiles(dir)
            list.forEach { f ->
                if(!Files.exists(f)) return@forEach
                val fName = f.fileName()
                list.filter { it.fileName(true).contains("$fName\\s*[0-9]+\\.po".toRegex()) && it !== f }.forEach outer@ {
                    if(!Files.exists(it)) return@outer
                    println(it.fileName())
                    val sb = StringBuilder(Files.readString(it))
                    sb.delete(0, sb.indexOf("#:"))
                    Files.writeString(f, sb, Charsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE)
                    Files.delete(it)
                }
            }
        }
*/
        val x = Paths.get("C:\\Users\\Whya5\\IdeaProjects\\EsoKR-Translate\\pot")
        Utils.listFiles(x).forEach {
            Files.move(it, it.parent.resolve("${it.fileName(true)}t"), StandardCopyOption.ATOMIC_MOVE)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<TestApplication>(*args)
        }
    }
}