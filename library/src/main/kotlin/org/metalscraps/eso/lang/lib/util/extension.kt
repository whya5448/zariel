@file:Suppress("RedundantVisibilityModifier", "unused")

package org.metalscraps.eso.lang.lib.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Path

private val logger: Logger = LoggerFactory.getLogger(Utils::class.java)

public fun String.toChineseOffset(): String {
    val c = this.toCharArray()
    for (i in c.indices) if (c[i].toInt() in 0xAC00..0xEA00) c[i] = c[i] - 0x3E00
    return String(c)
}

public fun String.toKorean(): String {
    val c = this.toCharArray()
    for (i in c.indices) if (c[i].toInt() in 0x6E00..0xAC00) c[i] = c[i] + 0x3E00
    return String(c)
}

public fun StringBuilder.toChineseOffset(): StringBuilder {
    for (i in this.indices) if (this[i].toInt() in 0xAC00..0xEA00) this[i] = this[i] - 0x3E00
    return this
}

public fun StringBuilder.toKorean(): StringBuilder {
    for (i in this.indices) if (this[i].toInt() in 0x6E00..0xAC00) this[i] = this[i] + 0x3E00
    return this
}

public fun Path.fileName(withExt: Boolean = false) : String {
    return when(withExt) {
        true -> this.fileName.toString()
        false -> {
            if (Files.isDirectory(this)) logger.warn("파일 아님 ${this.toAbsolutePath()}")
            val x = this.fileName.toString()
            x.substring(0, x.lastIndexOf('.'))
        }
    }
}

public fun Path.ext() : String {
    if (Files.isDirectory(this)) logger.warn("파일 아님 ${this.toAbsolutePath()}")
    val x = this.fileName.toString()
    return x.substring(x.lastIndexOf('.') + 1)
}

public fun ByteArray.toHexString() = toUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
public fun ByteArray.copyInt(offset: Int) = this.copyOfRange(offset, offset+4).toHexString().toInt(16)