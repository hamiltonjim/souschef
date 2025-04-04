/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Interface to the preferences database table.
 */
@Hidden
interface PreferenceDao : JpaRepository<Preference, Long> {
    /**
     * Get all preferences for the requesting [host] (IP or host name, whichever a request had)
     */
    fun findAllByHost(host: String): List<Preference>

    /**
     * Find a particular preference [key] for the given [host].
     */
    fun findByHostAndKey(host: String, key: String): Optional<Preference>
}