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

@Suppress("NO_NOARG_CONSTRUCTOR_IN_SUPERCLASS")
@Entity(name = "volumes")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
class Volume(
    override var name: String,
    inMl: Double,
    override var intl: Boolean,
    override var abbrev: String? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) override var id: Long? = null
) : UnitBase(name, inMl, intl, abbrev, id) {
    constructor(unit: AUnit) : this(unit.name, unit.inBase, unit.intl, unit.abbrev, unit.id)
}

