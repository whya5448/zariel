package org.metalscraps.eso.lang.lib.config

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * Created by 안병길 on 2018-01-17.
 * Whya5448@gmail.com
 */


object AppConfig {
    val CHARSET: Charset = StandardCharsets.UTF_8
    val POPattern: Pattern = Pattern.compile("(#, fuzzy)?\\n?msgctxt \"([0-9-]+)()()()\"\\n*?msgid \"{1,2}?\\n?([\\s\\S]*?)\"\\n*?msgstr \"{1,2}?\\n?([\\s\\S]*?)\"\\n{2,}", Pattern.MULTILINE)
    val CSVPattern: Pattern = Pattern.compile("\"()(([\\d]+?)-([\\d]+?)-([\\d]+?))\",\"([\\s\\S]*?)\",\"([\\s\\S]*?)\"\n", Pattern.MULTILINE)
    val CSVOffsetPattern: Pattern = Pattern.compile("\"(\\d+)\",\"(\\d+)\",\"(\\d+)\",\"(\\d+)\",\"(.*?)\"", Pattern.MULTILINE)
    val CategoryConfig: Pattern = Pattern.compile("FileName:(.*)((\\r\\n)|(\\n))isDuplicate:(.*)((\\r\\n)|(\\n))type:(.*)((\\r\\n)|(\\n))indexLinkCount:(.*)((\\r\\n)|(\\n))index:(.*)((\\r\\n)|(\\n))", Pattern.MULTILINE)
    const val ZANATA_DOMAIN = "https://translate.zanata.org/"
    val PATTERN_DESTINATION: Pattern = Pattern.compile("(\\[)(\\d+)(] = \\{\")(.+?)(\"},)")
}