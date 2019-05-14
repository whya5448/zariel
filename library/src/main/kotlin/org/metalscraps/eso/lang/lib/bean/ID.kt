package org.metalscraps.eso.lang.lib.bean

import com.fasterxml.jackson.annotation.JsonIgnore

class ID(var head: String, val tail: String) {

    @JsonIgnore
    fun isFileNameHead() : Boolean { return head.substring(0, 1).matches("^[a-zA-Z]".toRegex()) }
    override fun toString(): String { return "$head-$tail" }
    class NotFileNameHead : Exception()

}
