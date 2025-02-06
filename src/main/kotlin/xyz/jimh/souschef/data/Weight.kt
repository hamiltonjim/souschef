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
)
