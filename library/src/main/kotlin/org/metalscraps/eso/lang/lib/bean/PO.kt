package org.metalscraps.eso.lang.lib.bean

import java.util.*


/**
 * Created by 안병길 on 2018-01-18.
 * Whya5448@gmail.com
 */

open class PO(private val id: String, var source: String, var target: String, var fileName:String = "Undefined") : Comparable<PO> {

    val id1: Int
    val id2: Int
    val id3: Int
    open var isFuzzy = false

    init {
        source = source.replace("$q\n$q".toRegex(), "")
        target = target.replace("$q\n$q".toRegex(), "")

        val ids = id.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        id1 = ids[0].toInt()
        id2 = ids[1].toInt()
        id3 = ids[2].toInt()

        if (target == "") this.target = source
        else if (source == "") this.source = target
    }

    fun getLengthForLang(writeFileName:Boolean = false, beta:Boolean = false) : Int { return getTextForLang(writeFileName, beta).size}
    fun getTextForLang(writeFileName:Boolean = false, beta:Boolean = false): ByteArray { return getText(writeFileName, beta).toByteArray() }
    fun getText(writeFileName:Boolean = false, beta:Boolean = false): String {
        return when {
            writeFileName -> "${fileName}_${id2}_${id3}_$target"
            beta -> target
            else -> if (isFuzzy || target.contains("-G-")) source else target
        }
    }

    fun toPOTFormat(): String {
        return "#: $id\n" +
                "msgctxt $q$id$q\n" +
                "msgid $q$source$q\n" +
                "msgstr $q$q\n\n"
    }


    override fun compareTo(other: PO): Int {
        val src = Integer.toString(other.id2) + other.id3
        val trg = Integer.toString(this.id2) + this.id3
        return if (src == trg) this.id1.compareTo(other.id1)
        else src.compareTo(trg)
    }

    override fun toString(): String { return "$fileName-$id-$isFuzzy,'$source','$target'" }

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