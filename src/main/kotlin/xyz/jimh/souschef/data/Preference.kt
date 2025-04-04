/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 * A single entry of a preference for this host. Note: there is no login, so
 * only the [host] is important.
 * @property host
 * @property key
 * @property value
 * @property id
 */
@Suppress("unused")
@Entity(name = "preferences")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Preference(
    var host: String,
    var key: String,
    var value: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
)