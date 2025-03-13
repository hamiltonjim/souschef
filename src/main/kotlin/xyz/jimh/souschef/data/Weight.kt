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
@Entity(name = "weights")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
class Weight(
    var name: String,
    var inGrams: Double,
    var intl: Boolean,
    var abbrev: String? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
) {
    constructor(unit: AUnit) : this(unit.name, unit.inBase, unit.intl, unit.abbrev, unit.id)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Weight

        if (name != other.name) return false
        if (inGrams != other.inGrams) return false
        if (intl != other.intl) return false
        if (abbrev != other.abbrev) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + inGrams.hashCode()
        result = 31 * result + intl.hashCode()
        result = 31 * result + (abbrev?.hashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }
}
