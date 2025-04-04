/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

/**
 * Interface for listening to broadcast data.
 */
interface Listener {
    /**
     * Receive broadcast data [value] with the given [name].
     */
    fun listen(name: String, value: Any)
}