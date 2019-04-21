package org.metalscraps.eso.lang.lib

import java.nio.file.Paths

class TestMain {
    companion object {
        @JvmStatic
        fun main(@Suppress("UnusedMainParameter") args: Array<String>) {
            val x = Paths.get("C:\\Users\\Whya5\\AppData\\Local\\dc_eso_client/.config")

            println(x)
            println(x.parent)
        }
    }
}