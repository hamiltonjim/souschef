/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.display

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.springframework.stereotype.Component
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Listener
import xyz.jimh.souschef.config.OListener
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.data.Preference

/**
 * Object that can read a resource file and return its contents as a String.
 * File contents are saved in a cache.
 */
@Component
object ResourceText: OListener() {

    internal var textMap = HashMap<String, String>()

    /**
     * On startup, binds this [Listener] to [Preferences] (as [Broadcaster])
     */
    @PostConstruct
    fun init() {
        Preferences.addListener(this)
    }

    /**
     * On shutdown, unbinds from [Preferences].
     */
    @PreDestroy
    fun destroy() {
        Preferences.removeListener(this)
    }

    /**
     * Listener for changes in [Preference] values.
     */
    override fun listen(name: String, value: Any, sender: Broadcaster) {
        super.listen(name, value, sender)
        if (name == "locale")
            textMap.clear()
    }

    /**
     * Gets any file in the class path, using its [filename]
     */
    fun get(filename: String): String {
        load(filename)
        return textMap[filename] ?: ""
    }

    /**
     * Gets any file in the static directory, using its partial path in [filename]
     */
    fun getStatic(filename: String): String {
        load("static/$filename")
        return textMap["static/$filename"] ?: ""
    }

    private fun load(filename: String) {
        if (!textMap.containsKey(filename)) {
            val text = ResourceText::class.java.classLoader.getResourceAsStream(filename)
                ?.bufferedReader()?.use(BufferedReader::readText) ?: ""
            if (text.isNotEmpty()) {
                textMap[filename] = text
            }
        }
    }

    /**
     * Returns the contents of the given resource file as a Base64 string. Pass the path
     * from classpath in [filename].
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun getBase64(filename: String): String {
        val byteArray = ResourceText::class.java.classLoader
            .getResourceAsStream(filename)
            ?.use(InputStream::readBytes)
        if (byteArray != null)
            return Base64.encode(byteArray)
        throw FileNotFoundException("File not found: $filename")
    }

    /**
     * Clears the file cache.
     */
    fun flush() {
        textMap.clear()
    }
}