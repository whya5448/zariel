package org.metalscraps.eso.lang.tool.vo

class Category(val resourceId:Int, val resourceName:String, var category:String = resourceName, val volume:Int =0) {
    override fun toString(): String {
        return "Category(resourceName='$resourceName', category='$category')"
    }
}


