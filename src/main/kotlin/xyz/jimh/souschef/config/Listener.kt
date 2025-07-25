/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import java.time.Instant
import mu.KotlinLogging

/**
 * Interface for listening to broadcast data.
 */
interface Listener {

    /**
     * Class that holds a broadcast message.
     * @property name message name
     * @property value message value
     * @property sender [Broadcaster] that sent the message
     * @property time when the message was received. Not part of equals() or hashCode().
     */
    data class Message(val name: String, val value: Any, val sender: Broadcaster) {
        val time: Instant = Instant.now()
    }

    /**
     * The last message received by a Listener
     */
    var lastMessage: Message?

    /**
     * Receive broadcast data [value] with the given [name].
     */
    fun listen(name: String, value: Any, sender: Broadcaster) {
        lastMessage = Message(name, value, sender)
    }
}

/**
 * Convenience class that implements the Listener interface. Used by child classes that do
 * not need to inherit from another class.
 */
open class OListener : Listener {
    private val kLogger = KotlinLogging.logger {}
    override var lastMessage: Listener.Message? = null

    override fun listen(name: String, value: Any, sender: Broadcaster) {
        lastMessage = Listener.Message(name, value, sender)
        super.listen(name, value, sender)
        kLogger.debug { "listen: $name=$value" }
    }
}