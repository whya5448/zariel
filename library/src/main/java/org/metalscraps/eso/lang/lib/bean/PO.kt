package org.metalscraps.eso.lang.lib.bean

import java.util.*

/**
 * Created by 안병길 on 2018-01-18.
 * Whya5448@gmail.com
 */

open class PO(private val id: String, var source: String, var target: String, var fileName:String = "Undefined") : Comparable<PO> {

    private val q = '"'
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

    fun toCSVFormat(writeSource:Boolean = false, writeFileName:Boolean = false, beta:Boolean = false): String {
        var translatedMsg: String

        if (writeFileName) translatedMsg = "${fileName}_${id2}_${id3}_$target"
        else if (beta) translatedMsg = target
        else {
            translatedMsg = target
            if (isFuzzy || target.contains("-G-")) translatedMsg = source
        }
        return "$q$id$q,$q${if(writeSource) source else ""}$q,$q$translatedMsg$q\n"
    }


    override fun compareTo(other: PO): Int {
        val src = Integer.toString(other.id2) + other.id3
        val trg = Integer.toString(this.id2) + this.id3
        return if (src == trg) this.id1.compareTo(other.id1)
        else src.compareTo(trg)
    }

    override fun toString(): String { return "$fileName-$id-$isFuzzy','$source','$target'" }

    companion object {
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