// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Suppress("unused")
@Entity(name = "volumes")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
class Volume(
    var name: String,
    var inMl: Double,
    var intl: Boolean,
    var abbrev: String? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
)
