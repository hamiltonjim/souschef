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
    @Id override var id: Long?,
    override var name: String,
    @Id @Enumerated(EnumType.STRING) var type: UnitType,
    override var inBase: Double,
    override var intl: Boolean,
    override var abbrev: String? = null,
) : UnitBase(name, inBase, intl, abbrev, id) {

    @Embeddable
    data class Ident(val id: Long, val type: UnitType) : Serializable
}
