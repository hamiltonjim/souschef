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
 * @property [arrays] A map that matches a key to a list of localized string
 */
data class LocaleStrings(val strings: Map<String, String>, val arrays: Map<String, List<String>> = emptyMap()) {

    /**
     * Loads directly from a [StringFileLoader] object.
     */
    constructor(loader: StringsFileLoader): this(loader.strings, loader.arrays)

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

    fun getArray(key: String): List<String> {
        return when (val value = arrays[key]) {
            null -> throw IllegalStateException("Key '$key' has no array!")
            else -> value
        }
    }

    companion object {
        /**
         * Loads translated strings from the translation [file].
         */
        fun from(file: File): LocaleStrings {
            val maps = StringsFileLoader().load(file)
            return LocaleStrings(maps)
        }
    }
}
