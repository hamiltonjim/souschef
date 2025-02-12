// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.display

object UrlBaser {
    fun baseUrl(sample: String, reqUrl: StringBuffer): String {
        val start = reqUrl.lastIndexOf(sample)
        if (start == -1) {
            return ""
        }
        return reqUrl.substring(0, start)
    }
}