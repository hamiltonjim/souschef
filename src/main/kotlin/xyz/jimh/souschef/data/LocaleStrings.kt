/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import java.io.File
import xyz.jimh.souschef.config.StringsFileLoader

/**
 * Class that holds translated [strings], for reference from base strings.
 * @property [strings] A map that matches a key to a localized string
 */
data class LocaleStrings(val strings: Map<String, String>) {
    /**
     * Gets the translated string for the reference string [key].
     * @throws IllegalStateException if there is no translation for [key]
     */
    fun get(key: String): String {
        return when (val value = strings[key]) {
            null -> throw IllegalStateException("Key '$key' has no value!")
            else -> value
        }
    }

    companion object {
        /**
         * Loads translated strings from the translation [file].
         */
        fun from(file: File): LocaleStrings {
            val map = StringsFileLoader.load(file)
            return LocaleStrings(map)
        }
    }
}
