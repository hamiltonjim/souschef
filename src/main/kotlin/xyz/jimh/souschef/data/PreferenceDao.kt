// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.data

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface PreferenceDao : JpaRepository<Preference, Long> {
    fun findAllByHost(host: String): List<Preference>
    fun findByHostAndKey(host: String, key: String): Optional<Preference>
}