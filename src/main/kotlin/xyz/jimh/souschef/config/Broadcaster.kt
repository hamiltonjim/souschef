/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

/**
 * Class that broadcasts generic messages to all of its listeners.
 */
open class Broadcaster {
    private val listeners = ArrayList<Listener>()

    /**
     * Adds the [listener] to the listeners list.
     */
    fun addListener(listener: Listener) {
        listeners += listener
    }

    /**
     * removes the [listener] from the list
     */
    fun removeListener(listener: Listener) {
        listeners -= listener
    }

    /**
     * Returns the number of listeners.
     */
    fun numListeners() = listeners.size

    /**
     * Sends the message to each listener. The message is named by [name],
     * and can be anything [data].
     */
    fun broadcast(name: String, data: Any) {
        listeners.forEach { it.listen(name, data) }
    }

    /**
     * Sends unnamed [data] to all listeners.
     */
    fun broadcast(data: Any) = broadcast(NO_NAME, data)

    /**
     * Object that provides private constants.
     */
    companion object {
        /**
         * "Name" for unnamed data.
         */
        const val NO_NAME = "NO NAME"
    }
}