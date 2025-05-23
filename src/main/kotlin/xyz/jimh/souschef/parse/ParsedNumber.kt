/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.parse

/**
 * Data class that holds the parts of a (supposed) number string as the integer part [whole] and the
 * fractional part [fraction].
 * @property whole String containing the integer part. May be empty.
 * @property fraction String containing the fractional part. May be empty.
 */
data class ParsedNumber(val whole: String, val fraction: String)
