package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "foods")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
class FoodItem(
    var name: String,
    var description: String? = null,
    var notes: String? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
)