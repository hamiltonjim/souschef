/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import java.time.Instant

/**
 * Interface for listening to broadcast data.
 */
interface Listener {

    /**
     * Class that holds a broadcast message.
     * @property name message name
     * @property value message value
     * @property time when the message was received. Not part of equals() or hashCode().
     */
    data class Message(val name: String, val value: Any) {
        val time: Instant = Instant.now()
    }

    /**
     * The last message received by a Listener
     */
    var lastMessage: Message?

    /**
     * Receive broadcast data [value] with the given [name].
     */
    fun listen(name: String, value: Any) {
        lastMessage = Message(name, value)
    }
}