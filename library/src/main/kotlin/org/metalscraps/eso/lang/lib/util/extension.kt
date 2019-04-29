@file:Suppress("RedundantVisibilityModifier", "unused")

package org.metalscraps.eso.lang.lib.util

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