/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import java.io.Serializable
import xyz.jimh.souschef.data.AUnit.Ident

@Entity(name = "units")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
@IdClass(Ident::class)
class AUnit(
    @Id var id: Long,
    var name: String,
    @Id @Enumerated(EnumType.STRING) var type: UnitType,
    var inBase: Double,
    @Suppress("unused") var intl: Boolean,
    var abbrev: String? = null,
) {

    @Embeddable
    class Ident(val id: Long, val type: UnitType) : Serializable
}
