/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.parse

/**
 * Data class that holds the parts of a (supposed) number string as the integer part [whole] and the
 * fractional part [fraction].
 */
data class ParsedNumber(val whole: String, val fraction: String)
