package org.metalscraps.eso.lang.test

import org.metalscraps.eso.lang.lib.util.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.Paths

@SpringBootApplication
class TestApplication : CommandLineRunner {
    private val logger: Logger = LoggerFactory.getLogger(TestApplication::class.java)

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

        val x = Paths.get("C:\\Users\\Whya5\\IdeaProjects\\EsoKR-Translate\\pot")
        Utils.listFiles(x).forEach {
            Files.move(it, it.parent.resolve("${it.fileName(true)}t"), StandardCopyOption.ATOMIC_MOVE)
        }

*/

//.filter { it.fileName(true).contains("book.po") }
        val x = Paths.get("D:\\EsoExtractData\\gamedata\\lang\\en.lang")
        val z = Utils.getPOMap(Utils.listFiles(Paths.get("C:\\Users\\Whya5\\IdeaProjects\\EsoKR-LANG\\po")), Utils.TextParseOptions(parseSource = true))

        val y = Utils.readLANG(x)
        y.forEach { z.putIfAbsent(it.getID(), it) }
        println(y.size)
        Utils.makeLANGwithLog(Paths.get("D:/kr.lang"), z.values.toList())
        Utils.makeLANGwithLog(Paths.get("D:/kb.lang"), z.values.toList(), beta = true)
        Utils.makeLANGwithLog(Paths.get("D:/tr.lang"), z.values.toList(), writeFileName = true)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<TestApplication>(*args)
        }
    }
}