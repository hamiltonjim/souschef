/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import java.io.File
import xyz.jimh.souschef.config.StringsFileLoader

data class LocaleStrings(val strings: Map<String, String>) {
    fun get(key: String): String {
        return when (val value = strings[key]) {
            null -> throw IllegalStateException("Key '$key' has no value!")
            else -> value
        }
    }

    companion object {
        fun from(file: File): LocaleStrings {
            val map = StringsFileLoader.load(file)
            return LocaleStrings(map)
        }
    }
}
