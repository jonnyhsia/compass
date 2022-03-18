package com.arch.jonnyhsia.compass.api

class PageKey(
    val scheme: String?,
    val name: String
) {

    override fun equals(other: Any?): Boolean {
        if (other !is PageKey) {
            // 非 PageKey 返回 false
            return false
        } else {
            // name 不同返回 false
            if (other.name != this.name) {
                return false
            }
            // scheme 不同且皆非通配符, 返回 false
            if (other.scheme != this.scheme &&
                other.scheme != "*" &&
                this.scheme != "*" &&
                other.scheme != "" &&
                this.scheme != ""
            ) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        // 为使协议通配符 "*" 能够起作用, 只有 name 参与 hash
        return name.hashCode()
    }
}