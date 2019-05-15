package org.metalscraps.eso.lang.lib.bean

import java.util.*


/**
 * Created by 안병길 on 2018-01-18.
 * Whya5448@gmail.com
 */

open class PO(
        val id1: Int,
        val id2: Int,
        val id3: Int,
        private var source: String,
        private var target: String,
        var isFuzzy:Boolean = false,
        var fileName:String = "Undefined"
) : Comparable<PO> {


    fun getID(): String { return "$id1-$id2-$id3" }
    fun getLengthForLang(writeFileName:Boolean = false, beta:Boolean = false) : Int { return getTextForLang(writeFileName, beta).size}
    fun getTextForLang(writeFileName:Boolean = false, beta:Boolean = false): ByteArray { return getText(writeFileName, beta).toByteArray() }
    fun getText(writeFileName:Boolean = false, beta:Boolean = false): String {
        return when {
            writeFileName -> "${fileName}_${id3}_$target"
            beta -> target
            isFuzzy -> source
            else -> target
        }
    }

    fun toPOTFormat(): String {
        return "#: ${getID()}\n" +
                "msgctxt $q${getID()}$q\n" +
                "msgid $q$source$q\n" +
                "msgstr $q$q\n\n"
    }


    override fun compareTo(other: PO): Int {
        val src = Integer.toString(other.id2) + other.id3
        val trg = Integer.toString(this.id2) + this.id3
        return if (src == trg) this.id1.compareTo(other.id1)
        else src.compareTo(trg)
    }

    override fun toString(): String { return "$fileName-$id3,'$source','$target'" }

    companion object {
        private const val q = '"'
        var comparator = Comparator<PO> { o1, o2 ->
            o1.run {
                when {
                    id1 != o2.id1 -> id1.compareTo(o2.id1)
                    id2 != o2.id2 -> id2.compareTo(o2.id2)
                    else -> id3.compareTo(o2.id3)
                }
            }
        }
    }
}