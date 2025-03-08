/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

/*
 * Broadcasts generic messages to all listeners.
 */
open class Broadcaster {
    private val listeners = ArrayList<Listener>()

    fun addListener(listener: Listener) {
        listeners += listener
    }

    fun removeListener(listener: Listener) {
        listeners -= listener
    }

    fun numListeners() = listeners.size

    fun broadcast(name: String, data: Any) {
        listeners.forEach { it.listen(name, data) }
    }
}