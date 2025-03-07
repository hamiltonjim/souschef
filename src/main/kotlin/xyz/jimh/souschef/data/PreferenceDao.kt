/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

@Hidden
interface PreferenceDao : JpaRepository<Preference, Long> {
    fun findAllByHost(host: String): List<Preference>
    fun findByHostAndKey(host: String, key: String): Optional<Preference>
}