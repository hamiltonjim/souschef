/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 * A single entry of a preference for this host. Note: there is no login, so
 * only the [host] is important.
 * @property host the host that chose this preference
 * @property key name of the preference
 * @property value value for [key]
 * @property id unique ID of this preference
 */
@Schema(description = "A single preference value")
@Entity(name = "preferences")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Preference(
    @field:Schema(description = "A host name or IP address to associate with a preference", example = "localhost")
    var host: String,
    @field:Schema(description = "The name of the preference", example = "key")
    var key: String,
    @field:Schema(description = "The value of the preference", example = "value")
    var value: String,
    @field:Schema(description = "The ID of the preference, assigned by the database", example = "13")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
)