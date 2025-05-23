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
data class LocaleStrings(
    val strings: MutableMap<String, String>,
    val arrays: MutableMap<String, List<String>> = mutableMapOf(),
) {

    /**
     * Loads directly from a [StringsFileLoader] object.
     */
    constructor(loader: StringsFileLoader): this(loader.strings, loader.arrays)

    /**
     * Adds additional localized strings and lists of strings from [more].
     */
    fun add(more: StringsFileLoader) {
        addStrings(more.strings)
        addArrays(more.arrays)
    }

    /**
     * Adds additional localized strings from [moreStrings].
     */
    fun addStrings(moreStrings: Map<String, String>) {
        strings.putAll(moreStrings)
    }

    /**
     * Adds additional lists of localized strings from [moreArrays].
     */
    fun addArrays(moreArrays: Map<String, List<String>>) {
        arrays.putAll(moreArrays)
    }

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

    /**
     * Gets the translated string list for the reference array [key].
     * @throws IllegalStateException if there is no string list for [key]
     */
    fun getArray(key: String): List<String> {
        return when (val value = arrays[key]) {
            null -> throw IllegalStateException("Key '$key' has no array!")
            else -> value
        }
    }

    /**
     * Object containing the necessary factory function.
     */
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
