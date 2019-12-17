package org.metalscraps.eso.lang.lib.config

import org.metalscraps.eso.lang.lib.bean.Lang
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * Created by 안병길 on 2018-01-17.
 * Whya5448@gmail.com
 */


object AppConfig {
    val langFiles = arrayOf(
            Lang("kr.lang", writeFileName = false, beta = false),
            Lang("kb.lang", writeFileName = false, beta = true),
            Lang("tr.lang", writeFileName = true, beta = false)
    )

    val CHARSET: Charset = StandardCharsets.UTF_8
    val POPattern: Pattern = Pattern.compile("(#, fuzzy)?(\\r\\n|\\r|\\n)?msgctxt \"([0-9-]+)\"(\\r\\n|\\r|\\n)*?msgid \"{1,2}?(\\r\\n|\\r|\\n)?([\\s\\S]*?)\"(\\r\\n|\\r|\\n)*?msgstr \"{1,2}?(\\r\\n|\\r|\\n)?([\\s\\S]*?)\"(\\r\\n|\\r|\\n){2,}", Pattern.MULTILINE)
    val CategoryConfig: Pattern = Pattern.compile("FileName:(.*)((\\r\\n)|(\\n))isDuplicate:(.*)((\\r\\n)|(\\n))type:(.*)((\\r\\n)|(\\n))indexLinkCount:(.*)((\\r\\n)|(\\n))index:(.*)((\\r\\n)|(\\n))", Pattern.MULTILINE)
    val PATTERN_DESTINATION: Pattern = Pattern.compile("(\\[)(\\d+)(] = \\{\")(.+?)(\"},)")
    const val ZANATA_DOMAIN = "https://translate.zanata.org/"
}