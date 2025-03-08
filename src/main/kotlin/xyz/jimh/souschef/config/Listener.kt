/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

interface Listener {
    fun listen(name: String, value: Any)
}