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

@Suppress("unused")
@Entity(name = "preferences")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
class Preference(
    var host: String,
    var key: String,
    var value: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
)